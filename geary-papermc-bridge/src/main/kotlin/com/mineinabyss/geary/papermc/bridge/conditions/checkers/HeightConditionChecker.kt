package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.location.HeightCondition
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Location

class HeightConditionChecker : CheckingListener() {
    private val Pointers.condition by get<HeightCondition>().on(target)

    private val Pointers.location by get<Location>().on(event)

    override fun Pointers.check(): Boolean =
        location.y.toInt() in condition.range
}
