package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.location.LightCondition
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Location
import org.bukkit.block.BlockFace

class LightConditionChecker : GearyListener() {
    private val TargetScope.condition by get<LightCondition>()

    private val EventScope.location by get<Location>()

    @Handler
    fun TargetScope.check(event: EventScope): Boolean {
        val block = event.location.block
        // Check the current block's light level or the one above if this block is solid
        val check =
            if (block.isSolid) block.getRelative(BlockFace.UP)
            else block
        return check.lightLevel in condition.range
    }
}
