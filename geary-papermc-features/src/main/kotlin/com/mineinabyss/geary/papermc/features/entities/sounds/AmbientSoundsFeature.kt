package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.features.feature

val AmbientSoundsFeature = feature("ambient-sounds") {
    onLoad {
        gearyPaper.configure {
            geary.playAmbientSounds()
            geary.silenceVanillaSounds()
        }
    }

    onEnable {
        listeners(
            OverrideMobSoundsListener()
        )
    }
}
