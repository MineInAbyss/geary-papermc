package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import kotlin.random.Random

@JvmInline
@Serializable
@SerialName("geary:chance")
value class Chance(val percentage: Double)

class ChanceChecker : CheckingListener() {
    private val Pointers.chance by get<Chance>().on(source)

    override fun Pointers.check(): Boolean {
        return Random.nextDouble() < chance.percentage
    }
}
