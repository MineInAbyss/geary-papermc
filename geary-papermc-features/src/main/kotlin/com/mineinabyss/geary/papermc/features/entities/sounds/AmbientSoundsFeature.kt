package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.features.listeners

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
