package com.mineinabyss.geary.papermc.bridge.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:condition.chance")
data class Chance(
    val percentage: Double
)
