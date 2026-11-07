package com.mineinabyss.geary.papermc.spawning.readers

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged

object SpawnPositionReader {
    fun spawnPositionFor(location: Location): SpawnPosition {
        val block = location.block
        val state = block.blockData
        val type = state.material
        return when {
            type == Material.WATER || state is Waterlogged && state.isWaterlogged -> SpawnPosition.WATER
            type == Material.LAVA -> SpawnPosition.LAVA
            // Water, lava and bubble columns are the only liquid blocks, so with the first two ruled out this matches Block.isLiquid
            type != Material.BUBBLE_COLUMN && block.isPassable && block.getRelative(BlockFace.DOWN).let { it.isSolid || it.type == Material.SNOW } -> SpawnPosition.GROUND
            type.isAir -> SpawnPosition.AIR
            else -> SpawnPosition.IN_BLOCK
        }
    }
}
