package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.idofront.features.listeners

val AmbientSoundsFeature = module("ambient-sounds") {
    gearyWorld {
        playAmbientSounds()
        silenceVanillaSounds()
    }
    listeners(OverrideMobSoundsListener())
}
