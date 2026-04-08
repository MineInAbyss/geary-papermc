package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.features.entities.prevent.breeding.PreventBreedingListener
import com.mineinabyss.geary.papermc.features.entities.prevent.interaction.PreventInteractionListener
import com.mineinabyss.geary.papermc.features.entities.prevent.regen.PreventRegenerationListener
import com.mineinabyss.geary.papermc.features.entities.prevent.riding.PreventRidingListener
import com.mineinabyss.idofront.features.listeners

val PreventEventsFeature = module("prevent-events") {
    listeners(
        PreventBreedingListener(),
        PreventInteractionListener(),
        PreventRegenerationListener(),
        PreventRidingListener(),
        PreventEnchantingListener(),
    )
}
