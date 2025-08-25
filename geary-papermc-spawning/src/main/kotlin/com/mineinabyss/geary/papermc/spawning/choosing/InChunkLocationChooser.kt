package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.helpers.launchWithTicket
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World

class InChunkLocationChooser(
    private val mobSpawner: MobSpawner,
    private val mainWorld: World,
) {

    suspend fun chooseSpotInChunk(chunkLoc: Location, config: SpreadSpawnConfig): Location? {
        val chunk = chunkLoc.chunk
        if (!chunkLoc.isWorldLoaded || !chunkLoc.isChunkLoaded || chunkLoc.world != mainWorld)
            return null
        val pos = chunk.launchWithTicket {
            repeat(config.spawnAttempts) {
                val candidate = getValidBlockOrNull(chunk, config)
                if (candidate != null)
                    return@launchWithTicket candidate
            }
            return@launchWithTicket null
        }
        return pos.await()
    }

    private fun getValidBlockOrNull(chunk: Chunk, config: SpreadSpawnConfig): Location? {
        val spawnPositionReader = SpawnPositionReader()
        val testloc = getRandomChunkCoord(chunk, config)
        val type = spawnPositionReader.spawnPositionFor(testloc)
        val entry: SpawnEntry = config.entry

        if (type != entry.position || !mobSpawner.checkSpawnConditions(entry, testloc))
            return null

        return testloc
    }

    // chose a random spot within the chunk
    private fun getRandomChunkCoord(chunk: Chunk, config: SpreadSpawnConfig): Location {
        val yRange = config.sectionMinY.. config.sectionMaxY
        val x = chunk.x * 16 + (0..15).random()
        val z = chunk.z * 16 + (0..15).random()
        val y = yRange.random()
        return Location(mainWorld, x.toDouble(), y.toDouble(), z.toDouble())
    }
}
