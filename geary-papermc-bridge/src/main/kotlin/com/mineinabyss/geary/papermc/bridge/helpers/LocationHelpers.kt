package com.mineinabyss.geary.papermc.bridge.helpers

import com.mineinabyss.geary.papermc.configlang.components.ConfigurableTargetLocation
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Gets the location of the target block the player associated with the entity is looking at.
 *
 * @param maxDist The maximum distance this can extend.
 * @param allowAir Whether to allow clicking on nothing.
 */
fun Player.atTargetBlock(
    conf: ConfigurableTargetLocation,
): Location? {
    with(conf) {
        val block =
            if (onFace) getLastTwoTargetBlocks(null, maxDist).first()
            else getTargetBlockExact(maxDist) ?: return null

        val secondBlock =
            if (onFace) getLastTwoTargetBlocks(null, maxDist).last()
            else getTargetBlockExact(maxDist) ?: return null
        if (!allowAir && block.isEmpty && secondBlock.isEmpty) return null

        return block.location
    }
}
