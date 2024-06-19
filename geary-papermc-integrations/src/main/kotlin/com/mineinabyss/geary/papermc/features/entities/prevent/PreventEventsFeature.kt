package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.features.entities.prevent.breeding.PreventBreedingListener
import com.mineinabyss.geary.papermc.features.entities.prevent.interaction.PreventInteractionListener
import com.mineinabyss.geary.papermc.features.entities.prevent.regen.PreventRegenerationListener
import com.mineinabyss.geary.papermc.features.entities.prevent.riding.PreventRidingListener
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.preventEventsFeature() {
    onPluginEnable {
        listeners(
            PreventBreedingListener(),
            PreventInteractionListener(),
            PreventRegenerationListener(),
            PreventRidingListener(),
        )
    }
}
