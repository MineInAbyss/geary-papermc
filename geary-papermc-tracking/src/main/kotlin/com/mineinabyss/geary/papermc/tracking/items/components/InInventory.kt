package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Added to items located in the player's inventory
 */
@Serializable
@SerialName("geary:in_inventory")
sealed class InInventory
