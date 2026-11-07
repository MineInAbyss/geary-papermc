package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.choosing.PlayerPositions
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.time.inWholeTicks
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.GameMode.SPECTATOR
import org.bukkit.plugin.Plugin
import java.util.EnumMap
import java.util.EnumSet
import java.util.UUID
import kotlin.random.Random

class SpawnTask(
    config: SpawnConfig,
    plugin: Plugin,
    logger: ComponentLogger,
    private val locationChooser: SpawnLocationChooser,
    private val spawnChooser: SpawnChooser,
    private val mobSpawner: MobSpawner,
    private val mobCaps: MobCaps,
) {
    private val runTimeTicks: Map<SpawnPosition, Long> = config.runTimes.mapValues { it.value.inWholeTicks.coerceAtLeast(1) }
    private val spawnablePositions = SpawnPosition.entries.filter { it in spawnChooser.positionsWithSpawns }
    private val lastRunTicks = HashMap<UUID, EnumMap<SpawnPosition, Long>>()
    private val spawnAttempts: Int = config.maxSpawnAttemptsPerPlayer
    private val spawnDelayTicks = config.spawnDelay.inWholeTicks.coerceAtLeast(1)

    val job: Job = Job().also { job ->
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            runCatching { run() }.onFailure {
                logger.e { "Spawn task failed: ${it.stackTraceToString()}" }
            }
        }, 20, spawnDelayTicks)
        job.invokeOnCompletion { task.cancel() }
    }

    fun run() {
        if (spawnablePositions.isEmpty()) return
        val currTick = Bukkit.getCurrentTick().toLong()
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { !it.isDead && it.gameMode != SPECTATOR }
        val onlineIds = onlinePlayers.mapTo(HashSet()) { it.uniqueId }
        lastRunTicks.keys.retainAll(onlineIds)
        val positions = PlayerPositions(onlinePlayers)

        onlinePlayers.forEach { player ->
            val lastRun = lastRunTicks.getOrPut(player.uniqueId) { staggeredLastRuns(currTick) }
            val attemptedPositions = spawnablePositions.filterTo(EnumSet.noneOf(SpawnPosition::class.java)) {
                currTick - lastRun.getValue(it) >= runTicksFor(it)
            }
            if (attemptedPositions.isEmpty()) return@forEach
            attemptedPositions.forEach { lastRun[it] = currTick }

            // Candidates are within maxDistance of the player, assumed inside playerCapRadius, so they share one scan
            val playerLoc = player.location
            val nearby = lazy { mobCaps.nearbyMobs(playerLoc) }
            repeat(spawnAttempts) {
                if (attemptedPositions.isEmpty()) return@forEach
                val spawnLoc = locationChooser.chooseSpawnLocationNear(positions, playerLoc) ?: return@repeat
                val type = SpawnPositionReader.spawnPositionFor(spawnLoc)

                if (attemptedPositions.remove(type)) {
                    mobSpawner.attemptSpawnAt(spawnLoc, type, nearby)
                }
            }
        }
    }

    private fun runTicksFor(position: SpawnPosition) = runTimeTicks.getOrDefault(position, 1L)

    // Random phases spread players over each run interval instead of every player spawning on the same tick
    private fun staggeredLastRuns(currTick: Long) = EnumMap<SpawnPosition, Long>(SpawnPosition::class.java).apply {
        spawnablePositions.forEach { put(it, currTick - Random.nextLong(runTicksFor(it))) }
    }
}
