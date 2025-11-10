package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.default

internal fun IdoCommand.items() {
    "give" {
        permission = "geary.items.give"
        executes.asPlayer().args(
            "item" to GearyArgs.item(),
            "amount" to Args.integer(min = 1).default { 1 },
            "other" to Args.otherPlayer(),
        ) { item, amount, player ->
            val gearyItems = player.world.toGeary().getAddon(ItemTracking)
            val key = item.get<PrefabKey>() ?: fail("Could not find item prefab: $item")
            val item = gearyItems.createItem(key) ?: fail("Failed to create item from $key")
            item.amount = amount.coerceIn(1, item.maxStackSize)
            player.inventory.addItem(item)
        }
    }
}
