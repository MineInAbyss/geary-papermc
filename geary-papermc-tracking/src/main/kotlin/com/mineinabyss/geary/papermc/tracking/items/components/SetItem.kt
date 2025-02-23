package com.mineinabyss.geary.papermc.tracking.items.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sets item properties for a geary item entity.
 */
@JvmInline
@Serializable
@SerialName("geary:set.item")
value class SetItem(val item: SerializableItemStack)
