package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Added to items located in one of the player's passive slots */
@Serializable
@SerialName("geary:in_passive")
sealed class InPassive
