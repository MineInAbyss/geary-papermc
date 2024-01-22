package com.mineinabyss.geary.papermc.tracking.items.components

import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("geary:set.item.ignored_properties")
data class SetItemIgnoredProperties(val ignore: Set<BaseSerializableItemStack.Properties>) {
    fun ignoreAsEnumSet(): EnumSet<BaseSerializableItemStack.Properties> =
        if (ignore.isEmpty()) EnumSet.noneOf(BaseSerializableItemStack.Properties::class.java)
        else EnumSet.copyOf(ignore)
}
