package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.query.query
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack

private val plainTextSerializer by lazy { PlainTextComponentSerializer.plainText() }

fun Geary.createItemMigrationListener() = observe<OnSet>()
    .involving(query<SetItem, ItemStack>())
    .exec { (setItem, item) ->
        val itemModel = setItem.item.takeIf { it.customModelData == null }?.itemModel
        val oldCmd = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
        val oldItemModel = item.getData(DataComponentTypes.ITEM_MODEL)

        if (itemModel != null && oldCmd != null && oldItemModel != itemModel)
            item.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA)

        setItem.item.toItemStack(applyTo = item)

        // Migrate display name -> item name
        val itemName = setItem.item.takeIf { it.itemName != null && it.customName == null }?.itemName
        val customName = item.getData(DataComponentTypes.CUSTOM_NAME)

        // if old item name matches new one, ignoring formatting, remove it
        if (itemName != null && customName != null && plainTextSerializer.serialize(customName) == plainTextSerializer.serialize(itemName))
            item.unsetData(DataComponentTypes.CUSTOM_NAME)
    }
