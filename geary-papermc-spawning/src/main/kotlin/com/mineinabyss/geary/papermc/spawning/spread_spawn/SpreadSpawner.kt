package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.spawning.choosing.InChunkLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpreadChunkChooser
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox

class SpreadSpawner(
    private val db: Database,
    private val world: World,
    private val configs: SpreadSpawnSectionsConfig,
    private val posChooser: InChunkLocationChooser,
    private val chunkChooser: SpreadChunkChooser,
    private val dao: SpawnLocationsDAO,
) {
    suspend fun spawnSpreadEntities() {
        val container: RegionContainer = WorldGuard.getInstance().platform.regionContainer
        val wgWorld: com.sk89q.worldedit.world.World = BukkitAdapter.adapt(world)
        val regions: RegionManager? = container.get(wgWorld)

        for ((regionName, config) in configs.sectionsConfig) {

            val region = regions?.getRegion(regionName) ?: run {
                println("Region $regionName not found in world ${world.name}")
                continue
            }

            val cuboidRegion: ProtectedCuboidRegion = region as? ProtectedCuboidRegion ?: run {
                println("Region $regionName is not a cuboid region in world ${world.name}")
                continue
            }

            val chunkLoc = chooseChunkInRegion(cuboidRegion, config) ?: continue // No valid chunk found
            val spawnPos = chooseSpotInChunk(chunkLoc, config) ?: continue // No valid position found in chunk
            db.write { dao.insertSpawnLocation(spawnPos, StoredEntity(config.entry.type.key)) }
        }
    }

    suspend fun chooseChunkInRegion(worldGuardRegion: ProtectedCuboidRegion, config: SpreadSpawnConfig): Location? {
        val boundingBox = getBBFromRegion(worldGuardRegion)
        return chunkChooser.chooseChunkInBB(boundingBox, config)
    }

    suspend fun chooseSpotInChunk(chunkLoc: Location, config: SpreadSpawnConfig): Location? {
        return posChooser.chooseSpotInChunk(chunkLoc, config)
    }

    private fun getBBFromRegion(region: ProtectedCuboidRegion): BoundingBox {
        val minLoc = BukkitAdapter.adapt(world, region.minimumPoint)
        val maxLoc = BukkitAdapter.adapt(world, region.maximumPoint)
        return BoundingBox.of(minLoc, maxLoc)
    }

    suspend fun getNBNear(location: Location, radius: Double): Int {
        val radiusSq = radius * radius
        val nearbySpawns = db.read {
            dao.getSpawnsNear(location, radius)
                .filter {
                    val loc = it.location
                    val dx = loc.x - location.x
                    val dy = loc.y - location.y
                    val dz = loc.z - location.z
                    (dx * dx + dy * dy + dz * dz) <= radiusSq
                }
                .map {
                    it.location.world = this@SpreadSpawner.world
                    it.location.chunk
                }
                .toSet()
        }
        return nearbySpawns.size
    }
}
