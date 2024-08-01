package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.papermc.features.entities.prevent.breeding.PreventBreedingListener
import com.mineinabyss.geary.papermc.features.entities.prevent.interaction.PreventInteractionListener
import com.mineinabyss.geary.papermc.features.entities.prevent.regen.PreventRegenerationListener
import com.mineinabyss.geary.papermc.features.entities.prevent.riding.PreventRidingListener
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.plugin.listeners

object PreventEventsFeature : GearyAddonWithDefault<PreventEventsFeature> {
    override fun PreventEventsFeature.install() {
        gearyPaper.plugin.listeners(
            PreventBreedingListener(),
            PreventInteractionListener(),
            PreventRegenerationListener(),
            PreventRidingListener(),
        )
    }

    override fun default() = this
}
