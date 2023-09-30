package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.location.LightCondition
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Location
import org.bukkit.block.BlockFace

class LightConditionChecker : CheckingListener() {
    private val Pointers.condition by get<LightCondition>().on(target)

    private val Pointers.location by get<Location>().on(event)

    override fun Pointers.check(): Boolean {
        val block = location.block
        // Check the current block's light level or the one above if this block is solid
        val check =
            if (block.isSolid) block.getRelative(BlockFace.UP)
            else block
        return check.lightLevel in condition.range
    }
}
