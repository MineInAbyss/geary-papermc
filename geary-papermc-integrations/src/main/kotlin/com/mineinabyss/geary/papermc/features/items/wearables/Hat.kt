package com.mineinabyss.geary.papermc.features.items.wearables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound

/**
 * Lets an item be worn as a hat.
 *
 * @param sound The sound to play when equipped.
 */
@Serializable
@SerialName("looty:hat")
class Hat(
    val sound: Sound = Sound.ITEM_ARMOR_EQUIP_NETHERITE
)
