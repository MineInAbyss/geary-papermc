package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.regions.GearyRegions
import com.mineinabyss.geary.papermc.regions.RegionCuboid
import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.conditions.IsOpenAreaCondition
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class SpreadSpawner(
    private val spawns: SpreadSpawnRepository,
    private val world: World,
    private val configs: SpreadEntityTypesConfig,
    private val index: SurfaceIndex,
    private val tracker: SpreadEntityTracker,
    private val mobSpawner: MobSpawner,
    private val logger: Logger,
    private val regConfig: SpawnLocationsUnified,
    private val regions: GearyRegions,
) {
    enum class Result { Spawned, Full, NoSurface, NoSpacing, NoValidSpot }

    enum class SpotCheck { Valid, BadGeometry, ConditionsFailed }

    class Section(
        val type: String,
        val regionName: String,
        val config: SpreadSpawnConfig,
        val enableAltSpawns: Boolean,
        val box: BoundingBox,
        val chunks: ChunkRect,
        val minY: Int,
        val maxY: Int,
    ) {
        val key = "$type/$regionName"

        fun containsXZ(x: Int, z: Int) = x >= box.minX && x < box.maxX && z >= box.minZ && z < box.maxZ

        fun contains(pos: SpreadPos) = box.contains(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

        fun queryBox(worldMinY: Int, worldMaxY: Int): BoundingBox {
            val radius = config.spreadRadius.toDouble()
            val verticalWeight = config.verticalWeight
            return if (verticalWeight > 0) box.clone().expand(radius, radius / verticalWeight, radius)
            else BoundingBox(box.minX - radius, worldMinY.toDouble(), box.minZ - radius, box.maxX + radius, worldMaxY.toDouble(), box.maxZ + radius)
        }

        companion object {
            /** Region corners are inclusive block coordinates, as WorldGuard reports them */
            fun of(
                type: String,
                regionName: String,
                config: SpreadSpawnConfig,
                enableAltSpawns: Boolean,
                min: SpreadPos,
                max: SpreadPos,
            ) = Section(
                type = type,
                regionName = regionName,
                config = config,
                enableAltSpawns = enableAltSpawns,
                box = BoundingBox(min.x.toDouble(), min.y.toDouble(), min.z.toDouble(), max.x + 1.0, max.y + 1.0, max.z + 1.0),
                chunks = ChunkRect(min.chunkX, min.chunkZ, max.chunkX, max.chunkZ),
                minY = maxOf(min.y, config.sectionMinY),
                maxY = minOf(max.y, config.sectionMaxY),
            )
        }
    }

    private val worldMinHeight = world.minHeight
    private val worldMaxHeight = world.maxHeight
    private val minPlayerDistSq = configs.minPlayerDistance * configs.minPlayerDistance
    private val backoff = HashMap<String, TimeMark>()
    private val reported = ConcurrentHashMap.newKeySet<String>()
    private val lastResults = ConcurrentHashMap<String, Result>()

    @Volatile
    private var sections: List<Section> = emptyList()

    suspend fun spawnSpreadEntities() {
        val resolved = resolveSections()
        sections = resolved
        index.updateAreas(resolved.map { it.chunks }.distinct())
        for (section in resolved) {
            attempt(section)?.let { lastResults[section.key] = it }
        }
    }

    fun sectionFor(category: String, pos: SpreadPos): Section? =
        currentSections().firstOrNull { it.type == category && it.contains(pos) }

    fun spawnsTypeAt(typeKey: String, pos: SpreadPos): Boolean =
        currentSections().any { it.contains(pos) && it.config.entryForType(typeKey) != null }

    private fun currentSections() = sections.ifEmpty { resolveSections().also { sections = it } }

    /** Main thread only */
    fun check(entry: SpawnEntry, location: Location): SpotCheck {
        if (SpawnPositionReader.spawnPositionFor(location) != entry.position) return SpotCheck.BadGeometry
        val openness = requiredOpenness(entry)
        if (openness > 0 && !IsOpenAreaCondition(openness).isMet(location)) return SpotCheck.BadGeometry
        return if (mobSpawner.conditionsPassOrReport(entry, location)) SpotCheck.Valid else SpotCheck.ConditionsFailed
    }

    suspend fun statusLines(): List<String> = sections.map { section ->
        val rows = spawns.countSpawnsInBB(world, section.box, listOf(section.type))
        val stats = index.stats(section.chunks, section.minY, section.maxY, requiredOpenness(section.config.entry))
        "${section.key}: $rows/${section.config.spawnCap} rows, ${stats.indexedChunks} chunks indexed, surface weight ${stats.weight.toLong()}, last result ${lastResults[section.key] ?: "none"}"
    }

    /** Null while the section is backing off */
    private suspend fun attempt(section: Section): Result? {
        backoff[section.key]?.let { if (it.hasNotPassedNow()) return null }
        val config = section.config
        val entry = config.chooseEntry(section.enableAltSpawns)
        val radius = config.spreadRadius.toDouble()

        val nearby = spawns.getSpawnsInBB(world, section.queryBox(worldMinHeight, worldMaxHeight), listOf(section.type))
        if (nearby.count { section.contains(it.pos) } >= config.spawnCap)
            return backOff(section, Result.Full, SHORT_BACKOFF)

        val blacklist = config.blacklistRegions.mapNotNull { id ->
            regConfig.unified[id] ?: run {
                warnOnce("Blacklist region $id not found")
                null
            }
        }
        val candidates = index.sample(
            rect = section.chunks,
            minY = section.minY,
            maxY = section.maxY,
            minOpenness = requiredOpenness(entry),
            excluded = { x, y, z ->
                !section.containsXZ(x, z) || blacklist.any { it.isInside(x.toDouble(), y.toDouble(), z.toDouble()) }
            },
            count = config.candidateCount,
        )
        if (candidates.isEmpty()) return backOff(section, Result.NoSurface, LONG_BACKOFF)

        val spaced = SpreadSampling.spacedCandidates(candidates, nearby.map { it.pos }, config.verticalWeight, radius * radius)
        if (spaced.isEmpty()) return backOff(section, Result.NoSpacing, LONG_BACKOFF)

        val chosen = withContext(gearyPaper.minecraftDispatcher) {
            spaced.asSequence().take(MAX_SPOT_CHECKS).firstOrNull { isUsable(entry, it) }
        } ?: return backOff(section, Result.NoValidSpot, SHORT_BACKOFF)

        val row = spawns.insertSpawnLocation(
            Location(world, chosen.x.toDouble(), chosen.y.toDouble(), chosen.z.toDouble()),
            StoredEntity(type = entry.type.key, category = section.type)
        )
        withContext(gearyPaper.minecraftDispatcher) {
            if (world.isChunkLoaded(chosen.chunkX, chosen.chunkZ) && world.getChunkAt(chosen.chunkX, chosen.chunkZ).isEntitiesLoaded)
                tracker.rowsLoaded(listOf(row))
        }
        return Result.Spawned
    }

    // Spots in unloaded chunks are validated by ListSpawnListener once a player comes near
    private fun isUsable(entry: SpawnEntry, pos: SpreadPos): Boolean {
        val location = Location(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        if (world.players.any { it.location.distanceSquared(location) < minPlayerDistSq }) return false
        if (!world.isChunkLoaded(pos.chunkX, pos.chunkZ)) return true
        return when (check(entry, location)) {
            SpotCheck.Valid -> true
            SpotCheck.BadGeometry -> false.also { index.reject(pos) }
            SpotCheck.ConditionsFailed -> false
        }
    }

    private fun backOff(section: Section, result: Result, delay: Duration): Result {
        backoff[section.key] = TimeSource.Monotonic.markNow() + delay
        return result
    }

    private fun requiredOpenness(entry: SpawnEntry): Int {
        val range = SpreadSampling.requiredOpenness(entry.conditions.flatMap { it.conditions }.filterIsInstance<Condition>())
        if (range > SurfaceScanner.MAX_OPENNESS)
            warnOnce("${entry.type.key} requires an open area of $range, the surface index only tracks up to ${SurfaceScanner.MAX_OPENNESS}")
        return range.coerceIn(0, SurfaceScanner.MAX_OPENNESS)
    }

    private fun IsOpenAreaCondition.isMet(location: Location): Boolean =
        ActionGroupContext().apply { this.location = location.clone() }.execute()

    private fun resolveSections(): List<Section> {
        return configs.types.flatMap { (type, sectionsConfig) ->
            sectionsConfig.sectionsConfig.mapNotNull { (regionName, config) ->
                val cuboid = when (val found = regions.cuboid(world, regionName)) {
                    is RegionCuboid.Found -> found
                    RegionCuboid.Missing -> {
                        warnOnce("Region $regionName not found in world ${world.name} (${regions.name})")
                        return@mapNotNull null
                    }
                    RegionCuboid.NotCuboid -> {
                        warnOnce("Region $regionName is not a cuboid region in world ${world.name} (${regions.name})")
                        return@mapNotNull null
                    }
                }
                // The surface index only holds ground positions, so other positions would fail validation and wipe it
                val usesAlt = sectionsConfig.enableAltSpawns && config.altSpawnChance > 0
                if (config.entry.position != SpawnPosition.GROUND || usesAlt && config.altSpawnEntry.position != SpawnPosition.GROUND) {
                    warnOnce("Spread section $regionName for $type must spawn on GROUND, skipping it")
                    return@mapNotNull null
                }
                Section.of(
                    type, regionName, config, sectionsConfig.enableAltSpawns,
                    SpreadPos(cuboid.minX, cuboid.minY, cuboid.minZ),
                    SpreadPos(cuboid.maxX, cuboid.maxY, cuboid.maxZ),
                )
            }
        }
    }

    private fun warnOnce(message: String) {
        if (reported.add(message)) logger.w { message }
    }

    private companion object {
        const val MAX_SPOT_CHECKS = 4
        val SHORT_BACKOFF = 10.seconds
        val LONG_BACKOFF = 30.seconds
    }
}
