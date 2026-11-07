package com.mineinabyss.geary.papermc.spawning.spread_spawn

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.regions.GearyRegions
import com.mineinabyss.geary.papermc.regions.RegionCuboid
import com.mineinabyss.geary.papermc.spawning.choosing.InChunkLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpreadChunkChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import java.lang.Math.random

class SpreadSpawner(
    private val spawns: SpreadSpawnRepository,
    private val world: World,
    private val configs: SpreadEntityTypesConfig,
    private val chunkChooser: SpreadChunkChooser,
    private val posChooser: InChunkLocationChooser,
    private val logger: Logger,
    private val regConfig: SpawnLocationsUnified,
    private val regions: GearyRegions,
) {
    suspend fun spawnSpreadEntities() {
        for ((type, spreadConfigs) in configs.types) {
            spawnAll@ for ((regionName, config) in spreadConfigs.sectionsConfig) {

                val cuboid = when (val found = regions.cuboid(world, regionName)) {
                    is RegionCuboid.Found -> found
                    RegionCuboid.Missing -> {
                        logger.w { "Region $regionName not found in world ${world.name} (${regions.name})" }
                        continue
                    }
                    RegionCuboid.NotCuboid -> {
                        logger.w { "Region $regionName is not a cuboid region in world ${world.name} (${regions.name})" }
                        continue
                    }
                }

                val chunkLoc = chooseChunkInRegion(cuboid, config, type) ?: continue

                val spawnPos = chooseSpotInChunk(chunkLoc, config) ?: continue

                for (blacklisted in config.blacklistRegions) {
                    val region: SpawnLocationConfig? = regConfig.unified[blacklisted]
                    if (region == null) {
                        logger.e { "Blacklist region $blacklisted not found" }
                        continue
                    }
                    if (region.isInside(spawnPos)) {
                        continue@spawnAll
                    }
                }
                val spawnedEntity = StoredEntity(
                    type = if (random() * 100 <= config.altSpawnChance) config.altSpawnEntry.type.key else config.entry.type.key,
                    category = type
                )
                if (spawnedEntity.type == config.altSpawnEntry.type.key) {
                    logger.v { "Spawn alt entity ${spawnedEntity.type}" }
                }
                val spread = spawns.insertSpawnLocation(spawnPos, spawnedEntity)
                spread.spawn()

            }
        }
    }

    private suspend fun chooseChunkInRegion(cuboid: RegionCuboid.Found, config: SpreadSpawnConfig, type: String): Location? {
        val boundingBox = BoundingBox(
            cuboid.minX.toDouble(), cuboid.minY.toDouble(), cuboid.minZ.toDouble(),
            cuboid.maxX.toDouble(), cuboid.maxY.toDouble(), cuboid.maxZ.toDouble(),
        )
        return chunkChooser.chooseChunkInBB(boundingBox, config, type)
    }

    private suspend fun chooseSpotInChunk(chunkLoc: Location, config: SpreadSpawnConfig): Location? = withContext(gearyPaper.minecraftDispatcher) {
        posChooser.chooseSpotInChunk(chunkLoc, config)
    }
}
