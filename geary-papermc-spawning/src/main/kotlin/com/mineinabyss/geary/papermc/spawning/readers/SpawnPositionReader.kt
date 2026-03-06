package com.mineinabyss.geary.papermc.spawning.readers

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.core.BlockPos
import net.minecraft.world.level.material.Fluids
import org.bukkit.Location

object SpawnPositionReader {
    fun spawnPositionFor(location: Location): SpawnPosition {
        val world = location.world.toNMS()
        val state = world.getBlockState(BlockPos(location.blockX, location.blockY, location.blockZ))
        return when {
            state.fluidState.`is`(Fluids.WATER) -> SpawnPosition.WATER
            state.fluidState.`is`(Fluids.LAVA) -> SpawnPosition.LAVA
            state.`moonrise$emptyCollisionShape`() && state.fluidState.isEmpty && world.getBlockState(BlockPos(location.blockX, location.blockY - 1, location.blockZ)).isSolidRender -> SpawnPosition.GROUND
            state.isAir -> SpawnPosition.AIR
            else -> SpawnPosition.IN_BLOCK
        }
    }
}
