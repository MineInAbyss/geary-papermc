package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.helpers.launchWithTicket
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import org.bukkit.Location
import org.bukkit.World

class InChunkLocationChooser(
    private val mobSpawner: MobSpawner,
) {

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
        val entry: SpawnEntry = config.entry

        // this works by having a list of conditions assigned to the entry, in this case, the is open area condition will be defined in the yaml file of tne entry, and thus called in the check spawn conditions
        if (type != entry.position || !mobSpawner.checkSpawnConditions(entry, testloc))
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
}