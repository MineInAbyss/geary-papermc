package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper

class AmbientSoundsFeature(context: FeatureContext) : Feature(context) {
    override fun enable() {
        gearyPaper.configure {
            geary.playAmbientSounds()
            geary.silenceVanillaSounds()
        }

        listeners(
            OverrideMobSoundsListener()
        )
    }
}

//val AmbientSoundsFeature = createAddon("ambient-sounds") {
//    onPluginEnable {
//        playAmbientSounds()
//        silenceVanillaSounds()
//        plugin.listeners(
//            OverrideMobSoundsListener()
//        )
//    }
//}
