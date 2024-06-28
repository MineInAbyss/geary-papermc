package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import org.bukkit.inventory.ItemStack

fun GearyModule.createItemMigrationListener() = observe<OnSet>()
    .involving(query<SetItem, ItemStack>())
    .exec { (setItem, item) ->
        setItem.item.toItemStack(applyTo = item)
    }
