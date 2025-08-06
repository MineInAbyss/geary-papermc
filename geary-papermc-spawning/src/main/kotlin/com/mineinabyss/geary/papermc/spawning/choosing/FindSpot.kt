package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.helpers.launchWithTicket
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import org.bukkit.Location
import org.bukkit.World

suspend fun FindSpotInChunk(loc: Location, tgs: SpreadSpawner): Location? {
    val chunk = loc.chunk
    if (loc.world ==null) return null
    val pos = chunk.launchWithTicket {

        for (i in 0..500) {
            val candidate = getValidBlockOrNull(loc, tgs)
            if (candidate != null)
                return@launchWithTicket candidate
          }
        return@launchWithTicket null
    }
    return pos.await()
}

fun getValidBlockOrNull(loc: Location, tgs: SpreadSpawner): Location? {
    val chunk = loc.chunk
    val spawnPositionReader = SpawnPositionReader()
    val testloc = getRandomChunkCoord(chunk.x, chunk.z, loc.world, tgs)
    val type = spawnPositionReader.spawnPositionFor(testloc)
    // check for config ig
    if (type != SpawnPosition.GROUND || !isOpenArea(testloc, tgs))
        return null
    return Location(tgs.world, testloc.x, testloc.y, testloc.z)
}

// chose a random spot within the chunk
fun getRandomChunkCoord(chunkX: Int, chunkZ: Int, world: World, spawner: SpreadSpawner): Location {
    val x = chunkX * 16 + (0..15).random()
    val z = chunkZ * 16 + (0..15).random()
    val y = spawner.chunkYRange.random()
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}


// check a 5x5 area above a block to check if it is an open area
fun isOpenArea(location: Location, spawner: SpreadSpawner): Boolean {
    val world = location.world ?: return false
    val chunk = location.chunk
    val chunkMinX = chunk.x * 16
    val chunkMinZ = chunk.z * 16
    val blockX = location.blockX
    val blockY = location.blockY
    val blockZ = location.blockZ
    for (x in -spawner.openAreaSize..spawner.openAreaSize) {
        val checkX = (blockX + x).coerceIn(chunkMinX, chunkMinX + 15)
        for (y in spawner.openAreaHeight) {
            val checkY = (blockY + y).coerceIn(spawner.chunkYRange.first, spawner.chunkYRange.last)
            for (z in -spawner.openAreaSize..spawner.openAreaSize) {
                val checkZ = (blockZ + z).coerceIn(chunkMinZ, chunkMinZ + 15)
                if (!world.getBlockAt(checkX, checkY, checkZ).isPassable) {
                    return false
                }
            }
        }
    }
    return true
}
