package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.features.feature
import com.mineinabyss.geary.addons.world
import com.mineinabyss.idofront.features.listeners

val AmbientSoundsFeature = feature("ambient-sounds") {
    onEnable {
        world {
            playAmbientSounds()
            silenceVanillaSounds()
        }
        listeners(OverrideMobSoundsListener())
    }
}
