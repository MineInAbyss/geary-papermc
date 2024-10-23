package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.helpers.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.IdoPlayerCommandContext

internal fun IdoCommand.items() {
    "give" {
        requiresPermission("geary.items.give")
        val itemKeyArg by ArgsMinecraft.namespacedKey().suggests {
            val gearyItems = gearyPaper.worldManager.global.getAddon(ItemTracking)
            suggest(gearyItems.prefabs.getKeys().filterPrefabs(suggestions.remaining))
        }
        playerExecutes { giveItem(itemKeyArg().asString(), 1) }

        val amount by Args.integer(min = 1)
        playerExecutes { giveItem(itemKeyArg().asString(), amount()) }
    }
}

private fun IdoPlayerCommandContext.giveItem(key: String, amount: Int) {
    val gearyItems = player.world.toGeary().getAddon(ItemTracking)
    val item = gearyItems.createItem(PrefabKey.of(key))
        ?: commandException("Failed to create item from $key")
    item.amount = amount.coerceIn(1, item.maxStackSize)
    player.inventory.addItem(item)
}
