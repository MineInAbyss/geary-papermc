package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.location.TimeCondition
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Location

class TimeConditionChecker : GearyListener() {
    private val TargetScope.condition by get<TimeCondition>()

    val EventScope.location by get<Location>()

    @Handler
    fun TargetScope.check(event: EventScope): Boolean {
        val time = event.location.world.time

        // support these two possibilities
        // ====max-----min====
        // ----min=====max----
        return with(condition) {
            if (min < max) time in min..max
            else time !in max..min
        }
    }
}
