package com.mineinabyss.geary.papermc.configlang.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:location.target")
class ConfigurableTargetLocation(
    val maxDist: Int = 3,
    val allowAir: Boolean = false,
    val onFace: Boolean = false,
)
