package com.mineinabyss.geary.papermc.bridge.actions

import org.bukkit.Location
import org.bukkit.Material

/**
 * Ignites blocks in a cube around a given location with a maximum size of 20
 */
fun Location.igniteBlocks(size: Int): Boolean {
    val coercedSize = size.coerceAtMost(20) // Any bigger and we should start using WorldEdit
    for (x in -coercedSize..coercedSize) {
        for (y in -coercedSize..coercedSize) {
            for (z in -coercedSize..coercedSize) {
                val igniteLocation = Location(
                    this.world,
                    this.x + x,
                    this.y + y,
                    this.z + z
                )
                if (igniteLocation.block.isEmpty) {
                    val belowLocation = igniteLocation.clone().subtract(0.0, -1.0, 0.0)
                    if (!(belowLocation.block.isEmpty || belowLocation.block.isLiquid))
                        igniteLocation.block.type = Material.FIRE
                }
            }
        }
    }
    return true
}
