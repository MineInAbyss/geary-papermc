package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:player_instanced_item
 *
 * Indicates a Geary item entity should exist once per player instead of once for each ItemStack in the inventory.
 */
@Serializable
@SerialName("geary:player_instanced_item")
class PlayerInstancedItem
