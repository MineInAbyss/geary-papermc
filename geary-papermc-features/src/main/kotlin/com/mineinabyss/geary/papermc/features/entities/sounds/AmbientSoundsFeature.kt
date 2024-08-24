package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext

class AmbientSoundsFeature(context: FeatureContext) : Feature(context) {
    override fun enable() {
        geary.run {
            playAmbientSounds()
            silenceVanillaSounds()
        }

        listeners(
            OverrideMobSoundsListener()
        )
    }
}
