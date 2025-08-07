package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.helpers.launchWithTicket
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import org.bukkit.Location
import org.bukkit.World

class InChunkLocationChooser {

    suspend fun chooseSpotInChunk(chunkLoc: Location, spawner: SpreadSpawner, config: SpreadSpawnConfig): Location? {
        val chunk = chunkLoc.chunk
        if (chunkLoc.world == null || chunkLoc.world != spawner.world) return null

        val pos = chunk.launchWithTicket {
            repeat(config.spawnAttempts) {
                val candidate = getValidBlockOrNull(chunkLoc, spawner, config)
                if (candidate != null)
                    return@launchWithTicket candidate
            }
            return@launchWithTicket null
        }
        return pos.await()
    }

    private fun getValidBlockOrNull(loc: Location, spawner: SpreadSpawner, config: SpreadSpawnConfig): Location? {
        val chunk = loc.chunk
        val spawnPositionReader = SpawnPositionReader()
        val testloc = getRandomChunkCoord(chunk.x, chunk.z, loc.world, config)
        val type = spawnPositionReader.spawnPositionFor(testloc)

        // this check could also check for the config
        if (type != SpawnPosition.GROUND || !isOpenArea(testloc, config))
            return null
        return Location(spawner.world, testloc.x, testloc.y, testloc.z)
    }

    // chose a random spot within the chunk
    private fun getRandomChunkCoord(chunkX: Int, chunkZ: Int, world: World, config: SpreadSpawnConfig): Location {
        val yRange = config.sectionMinY.. config.sectionMaxY
        val x = chunkX * 16 + (0..15).random()
        val z = chunkZ * 16 + (0..15).random()
        val y = yRange.random()
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }


    // check a 5x5 area above a block to check if it is an open area
    private fun isOpenArea(location: Location, config: SpreadSpawnConfig): Boolean {
        val world = location.world ?: return false
        val chunk = location.chunk
        val chunkMinX = chunk.x * 16
        val chunkMinZ = chunk.z * 16
        val blockX = location.blockX
        val blockY = location.blockY
        val blockZ = location.blockZ

        for (x in -config.openAreaWidth..config.openAreaWidth) {
            val checkX = (blockX + x).coerceIn(chunkMinX, chunkMinX + 15)
            for (y in 0..config.openAreaHeight) {
                val checkY = (blockY + y).coerceIn(config.sectionMinY, config.sectionMaxY)
                for (z in -config.openAreaWidth..config.openAreaWidth) {
                    val checkZ = (blockZ + z).coerceIn(chunkMinZ, chunkMinZ + 15)
                    if (!world.getBlockAt(checkX, checkY, checkZ).isPassable) {
                        return false
                    }
                }
            }
        }
        return true
    }
}