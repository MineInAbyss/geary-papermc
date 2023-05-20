package com.mineinabyss.geary.papermc.bridge.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:explosion")
data class Explosion(
    val power: Float = 4F,
    val setFire: Boolean = false,
    val breakBlocks: Boolean = false,
    val fuseTicks: Int = 0
)

class Explode

