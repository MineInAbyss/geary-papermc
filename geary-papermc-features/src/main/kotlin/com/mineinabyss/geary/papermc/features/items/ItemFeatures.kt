package com.mineinabyss.geary.papermc.features.items

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.submodule
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDropListener
import com.mineinabyss.geary.papermc.features.items.holdsentity.SpawnHeldPrefabListener
import com.mineinabyss.geary.papermc.features.items.lightsource.LightSourceFeature
import com.mineinabyss.geary.papermc.features.items.nointeraction.DisableItemInteractionsListener
import com.mineinabyss.geary.papermc.features.items.passive.PassiveMenuListener
import com.mineinabyss.geary.papermc.features.items.posteffects.PostEffectsFeature
import com.mineinabyss.geary.papermc.features.items.repair.RepairKitListener
import com.mineinabyss.geary.papermc.features.items.transform.TransformOnPickupListener
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import com.mineinabyss.geary.papermc.tracking.geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.success

val CustomItemsFeature = module("custom-items") {
    require(get<GearyPaperConfig>().items.enabled) { "Items must be enabled in config" }

    submodule(PostEffectsFeature)
    submodule(LightSourceFeature)

    listeners(
        SpawnHeldPrefabListener(),
        DisableItemInteractionsListener(),
        ReplaceBurnedDropListener(),
        TransformOnPickupListener(),
        RepairKitListener(),
        PassiveMenuListener(),
    )
    PassiveSlots.menuEnabled = true
    addCloseable { PassiveSlots.menuEnabled = false }
}.mainCommand {
    "passive" {
        "slots" {
            permission = "geary.admin.passive"
            executes.args(
                "slots" to Args.integer(min = 0, max = PassiveSlots.COUNT),
                "other" to Args.otherPlayer(),
            ) { slots, player ->
                if (!PassiveSlots.setUnlocked(player, slots)) fail("Could not change passive slots for ${player.name}")
                sender.success("<yellow>${player.name}</yellow> now has <aqua>$slots</aqua> passive slots unlocked")
            }
        }
    }
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
            val leftover = player.inventory.addItem(item).values.sumOf { it.amount }
            val given = item.amount - leftover
            if (given <= 0) fail("<yellow>${player.name}</yellow>'s inventory is full, gave no <gold>$key</gold>")
            sender.success("Gave <yellow>${player.name}</yellow> <aqua>>$given</aqua> <gold>$key</gold>" + if (leftover > 0) ", <aqua>$leftover</aqua> did not fit" else "")
        }
    }
}
