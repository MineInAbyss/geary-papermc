package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.choosing.PlayerPositions
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnCandidateIndex
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.NearbyMobs
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.WorldMobs
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.time.inWholeTicks
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.EnumSet
import java.util.IdentityHashMap
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.random.Random

class SpawnTask(
    config: SpawnConfig,
    plugin: Plugin,
    logger: ComponentLogger,
    private val locationChooser: SpawnLocationChooser,
    private val spawnChooser: SpawnChooser,
    private val mobSpawner: MobSpawner,
    private val mobCaps: MobCaps,
    candidateIndex: SpawnCandidateIndex,
) : Listener {
    // Indexed by SpawnPosition ordinal
    private val runTimeTicks = LongArray(SpawnPosition.entries.size) { ordinal ->
        config.runTimes[SpawnPosition.entries[ordinal]]?.inWholeTicks?.coerceAtLeast(1) ?: 1L
    }
    private val spawnablePositions = SpawnPosition.entries.filter { it in spawnChooser.positionsWithSpawns }
    private val players = HashMap<UUID, PlayerState>()
    private val spawnAttempts: Int = config.maxSpawnAttemptsPerPlayer
    private val spawnDelayTicks = config.spawnDelay.inWholeTicks.coerceAtLeast(1)
    private val candidateIndex = candidateIndex.takeIf { config.indexSpawnPositions }
    private val maxCappedBackoff = config.maxCappedBackoff.coerceAtLeast(1)

    // Spawn locations stay within these block offsets of the player, the vertical one includes the highest block snap in SpawnLocationChooser
    private val boxHorizontal = maxOf(config.range.minDistance, config.range.maxDistance)
    private val boxVertical = maxOf(config.range.minDistance, config.range.maxVerticalDistance) + config.range.maxVerticalDistance + 2

    // Indexed by a bitmask of position ordinals.
    // A spawn type's category can fail to resolve until its backing mob is registered, so an entry is only kept once all of its categories resolve
    private val resolvedCategories = arrayOfNulls<Set<SpawnCategory>>(1 shl SpawnPosition.entries.size)

    private data class RegionCategoryKey(val regions: List<String>, val mask: Int)

    private val regionCategories = HashMap<RegionCategoryKey, Set<SpawnCategory>>()

    // lastRun is indexed by SpawnPosition ordinal and only filled for spawnablePositions
    private class PlayerState(val lastRun: LongArray) {
        var backoff: CappedBackoff? = null
    }

    private class DuePlayer(val player: Player, val state: PlayerState, val positions: EnumSet<SpawnPosition>)

    // Where and when a capped player was last pushed back, the delay is dropped once they leave the area their caps were counted in
    private class CappedBackoff(
        val streak: Int, val world: World, val x: Double, val y: Double, val z: Double,
        val tick: Long, val positions: EnumSet<SpawnPosition>,
    )

    var lastRunPlayers = 0
        private set
    var lastRunCappedSkips = 0
        private set
    var totalCappedSkips = 0L
        private set

    private val capCounter: ExecutorService? = config.capCountThreads.takeIf { it > 0 }?.let { threads ->
        val ids = AtomicInteger()
        Executors.newFixedThreadPool(threads) { runnable ->
            Thread(runnable, "Geary spawn cap counter ${ids.incrementAndGet()}").apply { isDaemon = true }
        }
    }

    val job: Job = Job().also { job ->
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            runCatching { run() }.onFailure {
                logger.e { "Spawn task failed: ${it.stackTraceToString()}" }
            }
        }, 20, spawnDelayTicks)
        job.invokeOnCompletion {
            task.cancel()
            capCounter?.shutdownNow()
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        players.remove(player.uniqueId)
    }

    fun run() {
        if (spawnablePositions.isEmpty()) return
        val currTick = Bukkit.getCurrentTick().toLong()
        // Every eligible player counts for the minimum distance, so their positions are read before anyone spawns.
        // NMS reads avoid CraftPlayer.getGameMode, which maps the game mode through a lookup on every call
        val positions = PlayerPositions()
        val due = ArrayList<DuePlayer>()
        for (player in Bukkit.getOnlinePlayers()) {
            val handle = (player as CraftPlayer).handle
            if (!handle.isAlive || handle.isSpectator) continue
            positions.add(handle.level().world, handle.x, handle.y, handle.z)

            val state = players.getOrPut(player.uniqueId) { PlayerState(staggeredLastRuns(currTick)) }
            if (state.backoff != null) dropBackoffIfMoved(state, handle.level().world, handle.x, handle.y, handle.z)
            val lastRun = state.lastRun
            var attempted: EnumSet<SpawnPosition>? = null
            for (position in spawnablePositions) {
                if (currTick - lastRun[position.ordinal] < runTicksFor(position)) continue
                if (attempted == null) attempted = EnumSet.noneOf(SpawnPosition::class.java)
                attempted.add(position)
                lastRun[position.ordinal] = currTick
            }
            if (attempted != null) due.add(DuePlayer(player, state, attempted))
        }
        var cappedSkips = 0
        val counted = capCounter?.let { CapCounts(due.size) }
        try {
            counted?.start(due, capCounter)
            for ((i, duePlayer) in due.withIndex()) {
                if (spawnFor(duePlayer, counted, i, positions, currTick)) cappedSkips++
            }
        } finally {
            counted?.finish()
        }
        lastRunPlayers = due.size
        lastRunCappedSkips = cappedSkips
        totalCappedSkips += cappedSkips
    }

    /** Returns true when the player was skipped for full caps */
    private fun spawnFor(duePlayer: DuePlayer, counted: CapCounts?, index: Int, positions: PlayerPositions, currTick: Long): Boolean {
        val attemptedPositions = duePlayer.positions
        val playerLoc: Location
        val nearbyMobs: NearbyMobs
        val categories: Set<SpawnCategory>
        // Nothing can spawn where categories are empty, which is not a full cap, so the player is not pushed back
        if (counted != null) {
            playerLoc = counted.locations[index]!!
            categories = counted.categories[index]!!
            if (categories.isEmpty()) return false
            nearbyMobs = counted.nearby(index)
        } else {
            // Candidates are within maxDistance of the player, assumed inside playerCapRadius, so they share one scan
            playerLoc = duePlayer.player.location
            nearbyMobs = mobCaps.nearbyMobs(playerLoc)
            categories = spawnCategoriesNear(playerLoc, attemptedPositions)
            if (categories.isEmpty()) return false
        }
        // Counts only rise during a run, so a player capped now cannot spawn anything in it
        if (mobCaps.allCapped(categories, nearbyMobs.categoryCounts)) {
            backOff(duePlayer, playerLoc, currTick)
            return true
        }
        duePlayer.state.backoff = null
        val nearby = lazyOf(nearbyMobs)
        repeat(spawnAttempts) {
            if (attemptedPositions.isEmpty()) return@repeat
            val index = candidateIndex?.takeIf { attemptedPositions.all { it == SpawnPosition.GROUND || it == SpawnPosition.WATER } }
            val spawnLoc = locationChooser.chooseSpawnLocationNear(positions, playerLoc, index) ?: return@repeat
            val type = SpawnPositionReader.spawnPositionFor(spawnLoc)

            if (attemptedPositions.remove(type)) {
                mobSpawner.attemptSpawnAt(spawnLoc, type, nearby)
            }
        }
        return false
    }

    /**
     * Counts caps for every due player of a run on the cap counter threads while the main thread spawns for the players before them.
     * The snapshots defer adds until [finish], so workers only read cells nothing writes to, and [nearby] adds the run's own spawns back on the main thread.
     * Locations, snapshots and categories are read here on the main thread in the same tick and before any spawn, so they match what the serial path reads
     */
    private inner class CapCounts(size: Int) {
        val locations = arrayOfNulls<Location>(size)
        val categories = arrayOfNulls<Set<SpawnCategory>>(size)
        private val snapshots = arrayOfNulls<WorldMobs>(size)
        private val tasks = arrayOfNulls<FutureTask<HashMap<SpawnCategory, Int>>>(size)
        private val deferred = IdentityHashMap<WorldMobs, Unit>()

        fun start(due: List<DuePlayer>, executor: ExecutorService) {
            val radius = mobCaps.searchRadius
            for (i in due.indices) {
                val location = due[i].player.location
                locations[i] = location
                val snapshot = mobCaps.snapshotFor(location.world)
                snapshots[i] = snapshot
                val near = spawnCategoriesNear(location, due[i].positions)
                categories[i] = near
                if (near.isEmpty()) continue
                if (deferred.put(snapshot, Unit) == null) snapshot.deferAdds()
                val x = location.x
                val y = location.y
                val z = location.z
                val task = FutureTask { snapshot.cellCounts(x, y, z, radius) }
                tasks[i] = task
                // A rejected count runs on the main thread in nearby instead
                runCatching { executor.execute(task) }
            }
        }

        // Runs the count here when no worker has taken it yet, otherwise waits for that worker
        fun nearby(index: Int): NearbyMobs {
            val task = tasks[index]!!
            task.run()
            val location = locations[index]!!
            return snapshots[index]!!.nearby(task.get(), location.x, location.y, location.z, mobCaps.searchRadius)
        }

        // Every count must have stopped reading before the deferred adds go into the cells
        fun finish() {
            for (task in tasks) {
                if (task == null) continue
                task.run()
                runCatching { task.get() }
            }
            deferred.keys.forEach { it.applyDeferredAdds() }
        }
    }

    /**
     * Categories of the spawn entries a run over [positions] could use anywhere around [location].
     * Spawn overrides and conditions only remove entries, so the union over every region touching the box is safe for the capped skip
     */
    private fun spawnCategoriesNear(location: Location, positions: Set<SpawnPosition>): Set<SpawnCategory> {
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        val regions = spawnChooser.regionSpawning.spawnRegionsOverlapping(
            location.world, x - boxHorizontal, y - boxVertical, z - boxHorizontal, x + boxHorizontal, y + boxVertical, z + boxHorizontal,
        ) ?: return spawnCategories(positions)
        val key = RegionCategoryKey(regions, maskOf(positions))
        regionCategories[key]?.let { return it }
        val entries = regions.flatMap { spawnChooser.regionSpawning.regionToSpawns[it].orEmpty() } + spawnChooser.customRegionSpawns
        val categories = categoriesOf(entries, positions) ?: return spawnCategories(positions)
        regionCategories[key] = categories
        return categories
    }

    /** Categories of the spawn entries at [positions], the only ones a run over those positions can spawn */
    private fun spawnCategories(positions: Set<SpawnPosition>): Set<SpawnCategory> {
        val mask = maskOf(positions)
        resolvedCategories[mask]?.let { return it }
        var allResolved = true
        val categories = spawnChooser.allSpawnEntries.filter { it.position in positions }.mapNotNullTo(HashSet()) { entry ->
            runCatching { entry.type.category }.getOrNull().also { if (it == null) allResolved = false }
        }
        if (allResolved) resolvedCategories[mask] = categories
        return categories
    }

    /** Null while any entry's category cannot be resolved yet */
    private fun categoriesOf(entries: List<SpawnEntry>, positions: Set<SpawnPosition>): Set<SpawnCategory>? {
        val categories = HashSet<SpawnCategory>()
        for (entry in entries) {
            if (entry.position !in positions) continue
            categories += runCatching { entry.type.category }.getOrNull() ?: return null
        }
        return categories
    }

    private fun maskOf(positions: Set<SpawnPosition>): Int {
        var mask = 0
        positions.forEach { mask = mask or (1 shl it.ordinal) }
        return mask
    }

    private fun runTicksFor(position: SpawnPosition) = runTimeTicks[position.ordinal]

    // Pushes the last run into the future, so the due check waits streak extra run times
    private fun backOff(duePlayer: DuePlayer, location: Location, currTick: Long) {
        if (maxCappedBackoff <= 1) return
        val state = duePlayer.state
        val streak = ((state.backoff?.streak ?: 0) + 1).coerceAtMost(maxCappedBackoff - 1)
        state.backoff = CappedBackoff(streak, location.world, location.x, location.y, location.z, currTick, EnumSet.copyOf(duePlayer.positions))
        for (position in duePlayer.positions) {
            state.lastRun[position.ordinal] = currTick + runTicksFor(position) * streak
        }
    }

    private fun dropBackoffIfMoved(state: PlayerState, world: World, x: Double, y: Double, z: Double) {
        val backoff = state.backoff ?: return
        val dx = x - backoff.x
        val dz = z - backoff.z
        val moved = world != backoff.world || dx * dx + dz * dz > boxHorizontal.toDouble() * boxHorizontal || abs(y - backoff.y) > boxVertical
        if (!moved) return
        state.backoff = null
        for (position in backoff.positions) {
            state.lastRun[position.ordinal] = minOf(state.lastRun[position.ordinal], backoff.tick)
        }
    }

    // Random phases spread players over each run interval instead of every player spawning on the same tick
    private fun staggeredLastRuns(currTick: Long) = LongArray(SpawnPosition.entries.size).apply {
        spawnablePositions.forEach { this[it.ordinal] = currTick - Random.nextLong(runTicksFor(it)) }
    }
}
