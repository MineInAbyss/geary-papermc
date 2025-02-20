package com.mineinabyss.geary.papermc.features.items

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDropListener
import com.mineinabyss.geary.papermc.features.items.holdsentity.SpawnHeldPrefabListener
import com.mineinabyss.geary.papermc.features.items.nointeraction.DisableItemInteractionsListener
import com.mineinabyss.geary.papermc.gearyPaper

class ItemFeatures(context: FeatureContext) : Feature(context) {
    override fun canEnable(): Boolean = gearyPaper.config.items.enabled

    override fun enable() {
        listeners(
            SpawnHeldPrefabListener(),
            DisableItemInteractionsListener(),
            ReplaceBurnedDropListener(),
        )
    }
}
