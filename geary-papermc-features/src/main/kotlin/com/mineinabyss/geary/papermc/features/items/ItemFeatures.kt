package com.mineinabyss.geary.papermc.features.items

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDropListener
import com.mineinabyss.geary.papermc.features.items.holdsentity.SpawnHeldPrefabListener
import com.mineinabyss.geary.papermc.features.items.nointeraction.DisableItemInteractionsListener
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.features.feature

val ItemsFeature = feature("items") {
    dependsOn {
        condition { get<GearyPaperConfig>().items.enabled }
    }

    onEnable {
        listeners(
            SpawnHeldPrefabListener(),
            DisableItemInteractionsListener(),
            ReplaceBurnedDropListener(),
        )
    }

    mainCommand {
        "give" {
            permission = "geary.items.give"
            executes.asPlayer().args(
                "item" to Args.geary.item(),
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
}
