package com.mineinabyss.geary.papermc.bridge.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.landed")
sealed class Landed
