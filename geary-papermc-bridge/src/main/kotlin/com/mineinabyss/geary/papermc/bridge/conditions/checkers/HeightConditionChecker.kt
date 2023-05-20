package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.location.HeightCondition
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Location

class HeightConditionChecker : GearyListener() {
    private val TargetScope.condition by get<HeightCondition>()

    private val EventScope.location by get<Location>()

    @Handler
    fun TargetScope.check(event: EventScope): Boolean =
        event.location.y.toInt() in condition.range
}
