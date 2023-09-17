package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.Chance
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlin.random.Random

class ChanceChecker : CheckingListener() {
    val Pointers.chance by get<Chance>().on(event)

    override fun Pointers.check(): Boolean {
        return Random.nextDouble() < chance.percentage
    }
}
