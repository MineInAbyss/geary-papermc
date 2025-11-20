package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.papermc.features.configureGeary
import com.mineinabyss.idofront.features.feature

val AmbientSoundsFeature = feature("ambient-sounds") {
    onEnable {
        configureGeary {
            autoClose(
                playAmbientSounds(),
                silenceVanillaSounds()
            )
        }

        listeners(
            OverrideMobSoundsListener()
        )
    }
}
