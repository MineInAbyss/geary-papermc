package com.mineinabyss.geary.papermc.spawning.readers

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.location.down
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Waterlogged

class SpawnPositionReader {
    fun spawnPositionFor(location: Location): SpawnPosition {
        val type = location.block.type
        val state = location.block.blockData
        return when {
            type == Material.WATER || state is Waterlogged && state.isWaterlogged -> SpawnPosition.WATER
            type == Material.LAVA -> SpawnPosition.LAVA
            location.clone().down(1).block.isSolid && location.block.isPassable && !location.block.isLiquid -> SpawnPosition.GROUND
            location.block.isEmpty -> SpawnPosition.AIR
            else -> SpawnPosition.IN_BLOCK
        }
    }
}
