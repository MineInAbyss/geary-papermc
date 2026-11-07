package com.mineinabyss.geary.papermc.spawning.listeners

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.spawn_types.EntityTypeNames
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadEntityTracker
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadPos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner.SpotCheck
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceIndex
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKey
import kotlinx.coroutines.delay
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.plugin.Plugin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// EntitiesLoadEvent rather than ChunkLoadEvent, since persisted entities are not in yet at chunk load
class ListSpawnListener(
    plugin: Plugin,
    private val logger: Logger,
    private val spawns: SpreadSpawnRepository,
    private val mainWorld: World,
    private val configs: SpreadEntityTypesConfig,
    private val tracker: SpreadEntityTracker,
    private val index: SurfaceIndex,
    private val spawner: SpreadSpawner,
) : Listener {
    private val spawnDistSq = configs.spawnDistance * configs.spawnDistance
    private val retryAfter = HashMap<Long, TimeMark>()

    // Chunks load and unload at the edge of view distance as players move, so spawning on load would respawn those mobs over and over
    val job = plugin.launchTickRepeating(20, gearyPaper.minecraftDispatcher) {
        runCatching { spawnNearPlayers() }.onFailure { logger.e { "Spawning spread mobs near players failed: ${it.stackTraceToString()}" } }
    }

    @EventHandler
    suspend fun EntitiesLoadEvent.onEntitiesLoad() {
        if (chunk.world != mainWorld) return
        val untagged = mutableListOf<LivingEntity>()
        for (entity in entities) {
            // Rows respawn their own entity, so any saved copy would be a duplicate
            if (tracker.tagOf(entity) != null) entity.remove()
            else if (entity is LivingEntity) untagged += entity
        }

        trackRows(chunk)
        removeUntagged(untagged)
    }

    @EventHandler
    fun EntitiesUnloadEvent.onEntitiesUnload() {
        if (chunk.world != mainWorld) return
        tracker.chunkUnloaded(chunkKey(chunk.x, chunk.z))
    }

    /**
     * Tracks spread mobs that outlived a reload, then the rows of chunks whose entities loaded before this listener existed.
     * Main thread only
     */
    suspend fun adoptLoadedEntities() {
        val tagged = mainWorld.entities.mapNotNull { entity -> tracker.tagOf(entity)?.let { entity to it } }
        val rows = spawns.getSpawnsWithIds(mainWorld, tagged.map { it.second.id }).associateBy { it.id }
        for ((entity, tag) in tagged) {
            if (!entity.isValid) continue
            rows[tag.id]?.takeIf { tracker.register(it, entity) } ?: entity.remove()
        }
        for (chunk in mainWorld.loadedChunks) {
            if (chunk.isEntitiesLoaded) trackRows(chunk)
        }
        removeUntagged(mainWorld.livingEntities.filter { tracker.tagOf(it) == null })
    }

    // Spread mobs spawned before tagging existed have no row to respawn from, so they are only removed
    // MythicMobs attaches its mob data after entities load, so the type is read a tick later
    private suspend fun removeUntagged(entities: List<Entity>) {
        if (entities.isEmpty()) return
        delay(1.ticks)
        for (entity in entities) {
            if (!entity.isValid || tracker.tagOf(entity) != null) continue
            val type = EntityTypeNames.of(entity) ?: continue
            val location = entity.location
            if (spawner.spawnsTypeAt(type, SpreadPos(location.blockX, location.blockY, location.blockZ))) entity.remove()
        }
    }

    private suspend fun trackRows(chunk: Chunk) {
        val stored = spawns.getSpawnsInChunk(chunk)

        // The entity section may have unloaded while the query ran
        if (!chunk.isEntitiesLoaded || !chunk.isLoaded) return
        tracker.rowsLoaded(stored)
    }

    private suspend fun spawnNearPlayers() {
        val players = mainWorld.players.map { it.location }
        if (players.isEmpty()) return
        retryAfter.values.removeIf { it.hasPassedNow() }
        val near = tracker.awaitingRows().filter { row ->
            row.id !in retryAfter && players.any { horizontalDistSq(it, row.pos) <= spawnDistSq }
        }
        if (near.isNotEmpty()) materialise(near)
    }

    // Entity tracking range is horizontal, so height is left out to match when players can see the mob
    private fun horizontalDistSq(player: Location, pos: SpreadPos): Double {
        val dx = player.x - pos.x
        val dz = player.z - pos.z
        return dx * dx + dz * dz
    }

    private suspend fun materialise(stored: List<SpreadSpawnLocation>) {
        val expiresBefore = Clock.System.now().epochSeconds - configs.clearSpawnsOlderThan.inWholeSeconds
        val invalid = mutableListOf<SpreadSpawnLocation>()
        for (spread in stored) {
            if (spread.createdTime < expiresBefore) continue
            when (check(spread)) {
                SpotCheck.Valid -> tracker.spawnIfAbsent(spread)
                SpotCheck.BadGeometry -> {
                    index.reject(spread.pos)
                    tracker.forget(spread.id, spread.createdTime)
                    invalid += spread
                }
                SpotCheck.ConditionsFailed -> retryAfter[spread.id] = TimeSource.Monotonic.markNow() + CONDITIONS_RETRY
            }
        }
        if (invalid.isEmpty()) return
        val ids = invalid.map { it.id }
        tracker.deleting(ids) { spawns.deleteSpawnLocations(mainWorld, ids) }
    }

    private fun check(spread: SpreadSpawnLocation): SpotCheck {
        val entry = spawner.sectionFor(spread.stored.category, spread.pos)?.config?.entryForType(spread.stored.type) ?: return SpotCheck.Valid
        return spawner.check(entry, spread.location)
    }

    private companion object {
        val CONDITIONS_RETRY = 30.seconds
    }
}
