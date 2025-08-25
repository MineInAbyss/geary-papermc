package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.MobSpawner
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
        if (!chunkLoc.isWorldLoaded || !chunkLoc.isChunkLoaded || chunkLoc.world != mainWorld) return null
        val chunk = chunkLoc.chunk

        return chunk.launchWithTicket {
            repeat(config.spawnAttempts) {
                val candidate = getValidBlockOrNull(chunk, config)
                if (candidate != null)
                    return@launchWithTicket candidate
            }
            return@launchWithTicket null
        }.await()
    }

    private fun getValidBlockOrNull(chunk: Chunk, config: SpreadSpawnConfig): Location? {
        val location = getRandomChunkCoord(chunk, config)
        val type = SpawnPositionReader.spawnPositionFor(location)

        if (type != config.entry.position || !mobSpawner.checkSpawnConditions(config.entry, location))
            return null

        return location
    }

    // chose a random spot within the chunk
    private fun getRandomChunkCoord(chunk: Chunk, config: SpreadSpawnConfig): Location {
        val x = chunk.x * 16 + (0..15).random()
        val z = chunk.z * 16 + (0..15).random()
        val y = (config.sectionMinY.. config.sectionMaxY).random()
        return Location(mainWorld, x.toDouble(), y.toDouble(), z.toDouble())
    }
}
