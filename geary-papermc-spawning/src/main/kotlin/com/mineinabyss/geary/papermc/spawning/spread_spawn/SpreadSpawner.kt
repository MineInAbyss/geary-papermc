package com.mineinabyss.geary.papermc.spawning.spread_spawn

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.InChunkLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpreadChunkChooser
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
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
) {
    suspend fun spawnSpreadEntities() {
        for ((type, spreadConfigs) in configs.types) {

            val container: RegionContainer = WorldGuard.getInstance().platform.regionContainer
            val wgWorld: com.sk89q.worldedit.world.World = BukkitAdapter.adapt(world)
            val regions: RegionManager? = container.get(wgWorld)

            for ((regionName, config) in spreadConfigs.sectionsConfig) {

                val region = regions?.getRegion(regionName) ?: run {
                    logger.w { "Region $regionName not found in world ${world.name}" }
                    continue
                }

                val cuboidRegion: ProtectedCuboidRegion = region as? ProtectedCuboidRegion ?: run {
                    logger.w { "Region $regionName is not a cuboid region in world ${world.name}" }
                    continue
                }

                val chunkLoc = chooseChunkInRegion(cuboidRegion, config, type) ?: continue

                val spawnPos = chooseSpotInChunk(chunkLoc, config) ?: continue

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

    private suspend fun chooseChunkInRegion(worldGuardRegion: ProtectedCuboidRegion, config: SpreadSpawnConfig, type: String): Location? {
        val boundingBox = getBBFromRegion(worldGuardRegion)
        return chunkChooser.chooseChunkInBB(boundingBox, config, type)
    }

    private suspend fun chooseSpotInChunk(chunkLoc: Location, config: SpreadSpawnConfig): Location? = withContext(gearyPaper.plugin.minecraftDispatcher) {
        posChooser.chooseSpotInChunk(chunkLoc, config)
    }

    private fun getBBFromRegion(region: ProtectedCuboidRegion): BoundingBox {
        val minLoc = BukkitAdapter.adapt(world, region.minimumPoint)
        val maxLoc = BukkitAdapter.adapt(world, region.maximumPoint)
        return BoundingBox.of(minLoc, maxLoc)
    }
}
