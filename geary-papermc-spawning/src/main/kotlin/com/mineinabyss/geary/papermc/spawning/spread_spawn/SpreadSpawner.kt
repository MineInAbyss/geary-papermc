package com.mineinabyss.geary.papermc.spawning.spread_spawn

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.InChunkLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpreadChunkChooser
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import kotlinx.coroutines.withContext
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import java.lang.Math.random
import kotlin.collections.get
import kotlin.text.compareTo
import kotlin.text.toInt
import kotlin.time.Duration
import kotlin.times

class SpreadSpawner(
    private val db: Database,
    private val world: World,
    private val configs: SpreadEntityTypesConfig,
    private val chunkChooser: SpreadChunkChooser,
    private val posChooser: InChunkLocationChooser,
    private val dao: SpawnLocationsDAO,
    private val logger: Logger,
) {
    suspend fun spawnSpreadEntities(task_nb: Int = 0) {
        val timings = mutableListOf<String>()
        val now = System.currentTimeMillis()
        logger.i { "Spawning spread entities at $now" }

        val totalStart = System.currentTimeMillis()
        for ((type, spreadConfigs) in configs.types) {
            val typeStart = System.currentTimeMillis()

            val container: RegionContainer = WorldGuard.getInstance().platform.regionContainer
            val wgWorld: com.sk89q.worldedit.world.World = BukkitAdapter.adapt(world)
            val regions: RegionManager? = container.get(wgWorld)

            for ((regionName, config) in spreadConfigs.sectionsConfig) {
                val regionStart = System.currentTimeMillis()

                val region = regions?.getRegion(regionName) ?: run {
                    logger.w { "Region $regionName not found in world ${world.name}" }
                    continue
                }

                val cuboidRegion: ProtectedCuboidRegion = region as? ProtectedCuboidRegion ?: run {
                    logger.w { "Region $regionName is not a cuboid region in world ${world.name}" }
                    continue
                }

                val chunkStart = System.currentTimeMillis()
                val chunkLoc = chooseChunkInRegion(cuboidRegion, config, type) ?: continue
                timings += "chooseChunkInRegion for $regionName took ${System.currentTimeMillis() - chunkStart}ms"

                val spotStart = System.currentTimeMillis()
                val spawnPos = chooseSpotInChunk(chunkLoc, config) ?: continue
                timings += "chooseSpotInChunk for $regionName took ${System.currentTimeMillis() - spotStart}ms"

                val entityStart = System.currentTimeMillis()
                val spawnedEntity = StoredEntity(
                    if (random() * 100 <= config.altSpawnChance) config.altSpawnEntry.type.key else config.entry.type.key,
                    type
                )
                timings += "Entity creation for $regionName took ${System.currentTimeMillis() - entityStart}ms"

                val dbStart = System.currentTimeMillis()
                val spread = db.write {
                    dao.insertSpawnLocation(spawnPos, spawnedEntity)
                }
                timings += "DB insert for $regionName took ${System.currentTimeMillis() - dbStart}ms"

                val spawnStart = System.currentTimeMillis()
                spread.spawn()
                timings += "Entity spawn for $regionName took ${System.currentTimeMillis() - spawnStart}ms"

                timings += "Region $regionName total took ${System.currentTimeMillis() - regionStart}ms"
            }
            timings += "Type $type total took ${System.currentTimeMillis() - typeStart}ms"
        }
        logger.i { "Finished spawning spread entities at ${System.currentTimeMillis()}" }
        logger.i { "total duration = ${System.currentTimeMillis() - now}ms" }
        logger.i { "task nb $task_nb" }
        timings += "Total spawnSpreadEntities took ${System.currentTimeMillis() - totalStart}ms"

// Print all timings at once
        timings.forEach { logger.i { it } }
    }


    suspend fun clearOldEntries(world: World, olderThan: Duration) = db.write {
        dao.deleteSpawnsOlderThan(world, olderThan)
    }

    suspend fun chooseChunkInRegion(worldGuardRegion: ProtectedCuboidRegion, config: SpreadSpawnConfig, type: String): Location? {
        val boundingBox = getBBFromRegion(worldGuardRegion)
        return chunkChooser.chooseChunkInBB(boundingBox, config, type)
    }

    suspend fun chooseSpotInChunk(chunkLoc: Location, config: SpreadSpawnConfig): Location? = withContext(gearyPaper.plugin.minecraftDispatcher) {
        posChooser.chooseSpotInChunk(chunkLoc, config)
    }

    private fun getBBFromRegion(region: ProtectedCuboidRegion): BoundingBox {
        val minLoc = BukkitAdapter.adapt(world, region.minimumPoint)
        val maxLoc = BukkitAdapter.adapt(world, region.maximumPoint)
        return BoundingBox.of(minLoc, maxLoc)
    }

    suspend fun countNearby(location: Location, radius: Double, type: String): Int = db.read {
        dao.countNearbyOfType(location, radius, type)
    }
}
