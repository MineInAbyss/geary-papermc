package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.editItemMeta
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack

private val plainTextSerializer by lazy { PlainTextComponentSerializer.plainText() }

fun Geary.createItemMigrationListener() = observe<OnSet>()
    .involving(query<SetItem, ItemStack>())
    .exec { (setItem, item) ->
        setItem.item.toItemStack(applyTo = item)

        // Migrate display name -> item name
        val newItem = setItem.item
        val oldCustomName = item.itemMeta.displayName()
        val newItemName = newItem.itemName

        // if old item name matches new one, ignoring formatting, remove it
        if (newItem.customName == null && newItemName != null && oldCustomName != null &&
            plainTextSerializer.serialize(oldCustomName) == plainTextSerializer.serialize(newItemName)
        ) item.editItemMeta {
            displayName(null)
        }
    }
