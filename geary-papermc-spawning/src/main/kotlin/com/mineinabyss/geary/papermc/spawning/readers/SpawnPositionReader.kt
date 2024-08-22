package com.mineinabyss.geary.papermc.spawning.readers

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.location.up
import org.bukkit.Location
import org.bukkit.Material

class SpawnPositionReader {
    fun spawnPositionFor(location: Location): SpawnPosition {
        val type = location.block.type
        return when {
            type == Material.WATER -> SpawnPosition.WATER
            type == Material.LAVA -> SpawnPosition.LAVA
            location.block.isEmpty -> SpawnPosition.AIR
            location.block.isSolid && location.up(1).block.isPassable -> SpawnPosition.GROUND
            else -> SpawnPosition.IN_BLOCK
        }
    }
}
