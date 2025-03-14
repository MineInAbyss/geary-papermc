package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Added to items equipped in an armor slot
 */
@Serializable
@SerialName("geary:equipped")
sealed class Equipped
