package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.inventory.ItemStack
import java.util.*

fun GearyModule.createItemMigrationListener() = observe<OnSet>()
    .involving(query<SetItem, ItemStack>())
    .exec { (setItem, item) ->
        val overrides = entity.get<SetItemIgnoredProperties>()
        val ignoredProperties =
            overrides?.ignoreAsEnumSet() ?: EnumSet.noneOf(BaseSerializableItemStack.Properties::class.java)
        val newItem = setItem.item.toItemStack(applyTo = item, ignoredProperties)
        item.type = newItem.type
        item.itemMeta = newItem.itemMeta
    }
