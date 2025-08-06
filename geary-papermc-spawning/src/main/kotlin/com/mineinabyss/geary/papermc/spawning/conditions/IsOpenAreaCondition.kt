package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:is_open_area")
class IsOpenAreaCondition(
    val range: Int = 5,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location ?: return false
        val world = location.world ?: return false
        val chunk = location.chunk
        val chunkMinX = chunk.x * 16
        val chunkMinZ = chunk.z * 16
        val blockX = location.blockX
        val blockY = location.blockY
        val blockZ = location.blockZ

        // check a 5x5 area above a block to check if it is an open area
        for (x in -range..range) {
            val checkX = (blockX + x).coerceIn(chunkMinX, chunkMinX + 15)
            for (y in 0..range) {
                val checkY = (blockY + y).coerceIn(chunk.world.minHeight, chunk.world.maxHeight)
                for (z in -range..range) {
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