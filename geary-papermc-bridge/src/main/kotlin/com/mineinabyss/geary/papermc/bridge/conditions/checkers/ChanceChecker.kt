package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.Chance
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import kotlin.random.Random

class ChanceChecker : GearyListener() {
    val EventScope.chance by get<Chance>()

    @Handler
    fun check(eventScope: EventScope): Boolean =
        Random.nextDouble() < eventScope.chance.percentage
}
