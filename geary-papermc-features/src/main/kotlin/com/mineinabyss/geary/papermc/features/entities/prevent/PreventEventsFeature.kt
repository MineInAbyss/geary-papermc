package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.features.entities.prevent.breeding.PreventBreedingListener
import com.mineinabyss.geary.papermc.features.entities.prevent.interaction.PreventInteractionListener
import com.mineinabyss.geary.papermc.features.entities.prevent.regen.PreventRegenerationListener
import com.mineinabyss.geary.papermc.features.entities.prevent.riding.PreventRidingListener

class PreventEventsFeature(context: FeatureContext) : Feature(context) {
    override fun enable() {
        listeners(
            PreventBreedingListener(),
            PreventInteractionListener(),
            PreventRegenerationListener(),
            PreventRidingListener(),
            PreventEnchantingListener(),
        )
    }
}
