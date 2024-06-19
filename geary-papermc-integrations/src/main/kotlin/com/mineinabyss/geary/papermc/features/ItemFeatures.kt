package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.features.items.backpack.BackpackListener
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDropListener
import com.mineinabyss.geary.papermc.features.items.holdsentity.SpawnHeldPrefabSystem
import com.mineinabyss.geary.papermc.features.items.nointeraction.DisableItemInteractionsListener
import com.mineinabyss.geary.papermc.features.items.wearables.WearableItemSystem
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.itemFeatures() {
    onPluginEnable {
        listeners(
            WearableItemSystem(),
            BackpackListener(),
            SpawnHeldPrefabSystem(),
            DisableItemInteractionsListener(),
            ReplaceBurnedDropListener(),
        )
    }
}
