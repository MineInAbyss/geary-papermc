package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > looty:player_instanced_item
 *
 * Indicates a Looty item entity should exist once per player instead of once for each ItemStack in the inventory.
 */
@Serializable
@SerialName("looty:player_instanced_item")
class PlayerInstancedItem
