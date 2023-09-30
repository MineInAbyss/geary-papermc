package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.location.TimeCondition
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Location

class TimeConditionChecker : CheckingListener() {
    private val Pointers.condition by get<TimeCondition>().on(target)

    val Pointers.location by get<Location>().on(event)

    override fun Pointers.check(): Boolean {
        val time = location.world.time

        // support these two possibilities
        // ====max-----min====
        // ----min=====max----
        return with(condition) {
            if (min < max) time in min..max
            else time !in max..min
        }
    }
}
