package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.context.IdoPlayerCommandContext
import com.mineinabyss.idofront.commands.brigadier.playerExecutes

internal fun IdoCommand.items() {
    "give" {
        requiresPermission("geary.items.give")
        playerExecutes(
            GearyArgs.item().named("item"),
            Args.integer(min = 1).default { 1 }.named("amount")
        ) { item, amount ->
            giveItem(item, amount)
        }
    }
}

private fun IdoPlayerCommandContext.giveItem(prefab: Entity, amount: Int) {
    val gearyItems = player.world.toGeary().getAddon(ItemTracking)
    val key = prefab.get<PrefabKey>() ?: fail("Could not find item prefab: $prefab")
    val item = gearyItems.createItem(key) ?: fail("Failed to create item from $key")
    item.amount = amount.coerceIn(1, item.maxStackSize)
    player.inventory.addItem(item)
}
