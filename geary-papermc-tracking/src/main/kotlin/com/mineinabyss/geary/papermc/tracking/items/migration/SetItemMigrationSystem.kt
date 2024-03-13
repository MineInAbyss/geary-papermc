package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.inventory.ItemStack
import java.util.*

fun GearyModule.createItemMigrationListener() = listener(
    object : ListenerQuery() {
        val setItem by get<SetItem>()
        val item by get<ItemStack>()
        val overrides by get<SetItemIgnoredProperties>().orNull()
        override fun ensure() = event.anySet(::setItem, ::item)
    }
).exec {
    val ignoredProperties =
        overrides?.ignoreAsEnumSet() ?: EnumSet.noneOf(BaseSerializableItemStack.Properties::class.java)
    setItem.item.toItemStack(applyTo = item, ignoredProperties)
}
