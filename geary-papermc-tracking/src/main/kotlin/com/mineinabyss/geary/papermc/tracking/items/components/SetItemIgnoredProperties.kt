package com.mineinabyss.geary.papermc.tracking.items.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("geary:set.item.ignored_properties")
data class SetItemIgnoredProperties(val ignore: Set<SerializableItemStack.Properties>) {
    fun ignoreAsEnumSet(): EnumSet<SerializableItemStack.Properties> =
        if (ignore.isEmpty()) EnumSet.noneOf(SerializableItemStack.Properties::class.java)
        else EnumSet.copyOf(ignore)
}
