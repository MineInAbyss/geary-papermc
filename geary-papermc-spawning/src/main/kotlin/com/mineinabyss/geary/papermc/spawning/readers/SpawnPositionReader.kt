package com.mineinabyss.geary.papermc.spawning.readers

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged

object SpawnPositionReader {
    fun spawnPositionFor(location: Location): SpawnPosition {
        val block = location.block
        val type = block.type
        val state = block.blockData
        return when {
            type == Material.WATER || state is Waterlogged && state.isWaterlogged -> SpawnPosition.WATER
            type == Material.LAVA -> SpawnPosition.LAVA
            block.isPassable && !block.isLiquid && block.getRelative(BlockFace.DOWN).let { it.isSolid || it.type == Material.SNOW } -> SpawnPosition.GROUND
            type.isAir -> SpawnPosition.AIR
            else -> SpawnPosition.IN_BLOCK
        }
    }
}
