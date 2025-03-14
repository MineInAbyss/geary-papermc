package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Added to items in the player's hand (only the main hand)
 */
@Serializable
@SerialName("geary:in_hand")
sealed class InHand
