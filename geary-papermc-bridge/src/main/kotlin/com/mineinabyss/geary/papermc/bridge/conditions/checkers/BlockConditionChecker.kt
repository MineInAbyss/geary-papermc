package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.BlockConditions
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Location

class BlockConditionChecker : GearyListener() {
    private val TargetScope.conditions by get<BlockConditions>()

    private val EventScope.location by get<Location>()

    @Handler
    fun TargetScope.check(event: EventScope): Boolean =
        event.location.block.type.let {
            (conditions.allow.isEmpty() || it in conditions.allow) && it !in conditions.deny
        }
}
