package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.idofront.features.feature

val AmbientSoundsFeature = feature("ambient-sounds") {
    configureGeary {
        onEnable {
            playAmbientSounds()
            silenceVanillaSounds()
        }
    }

    onEnable {
        listeners(OverrideMobSoundsListener())
    }
}
