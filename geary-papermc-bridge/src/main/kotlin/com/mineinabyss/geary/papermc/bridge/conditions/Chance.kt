package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@JvmInline
@Serializable
@SerialName("geary:chance")
value class Chance(val percentage: Double)

fun GearyModule.createChanceChecker() = listener(object : ListenerQuery() {
    val chance by source.get<Chance>()
}).check { Random.nextDouble() < chance.percentage }
