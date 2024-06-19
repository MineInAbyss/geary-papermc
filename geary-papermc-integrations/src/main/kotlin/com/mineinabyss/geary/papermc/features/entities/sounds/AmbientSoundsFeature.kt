package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.ambientSoundsFeature() {
    playAmbientSounds()
    silenceVanillaSounds()

    onPluginEnable {
        listeners(
            OverrideMobSoundsListener()
        )
    }
}
