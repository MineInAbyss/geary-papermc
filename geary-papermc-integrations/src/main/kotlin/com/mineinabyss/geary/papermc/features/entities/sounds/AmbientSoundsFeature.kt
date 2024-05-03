package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.plugin.listeners

open class AmbientSoundsFeature {
    companion object : GearyAddonWithDefault<AmbientSoundsFeature> {
        override fun AmbientSoundsFeature.install() {
            geary.run {
                playAmbientSounds()
                silenceVanillaSounds()
            }
            gearyPaper.plugin.listeners(
                OverrideMobSoundsListener()
            )
        }

        override fun default() = AmbientSoundsFeature()
    }
}
