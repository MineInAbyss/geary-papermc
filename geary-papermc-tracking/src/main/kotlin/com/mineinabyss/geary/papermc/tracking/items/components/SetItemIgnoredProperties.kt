package com.mineinabyss.geary.papermc.tracking.items.components

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
@SerialName("geary:set.item.ignored_properties")
class SetItemIgnoredProperties(private val _ignore: Set<SerializableItemStack.Properties>) {
    @Transient
    val ignore: EnumSet<SerializableItemStack.Properties> =
        if (_ignore.isEmpty()) EnumSet.noneOf(SerializableItemStack.Properties::class.java)
        else EnumSet.copyOf(_ignore)
}
