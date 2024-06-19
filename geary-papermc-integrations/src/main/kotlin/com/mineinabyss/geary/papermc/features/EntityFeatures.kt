package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.features.entities.bucketable.BucketableListener
import com.mineinabyss.geary.papermc.features.entities.displayname.ShowDisplayNameOnKillerListener
import com.mineinabyss.geary.papermc.features.entities.prevent.preventEventsFeature
import com.mineinabyss.geary.papermc.features.entities.sounds.ambientSoundsFeature
import com.mineinabyss.geary.papermc.features.entities.taming.TamingListener
import com.mineinabyss.geary.papermc.features.general.cooldown.cooldownDisplaySystem
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.entityFeatures() {
    preventEventsFeature()
    ambientSoundsFeature()
    cooldownDisplaySystem()

    onPluginEnable {
        listeners(
            BucketableListener(),
            ShowDisplayNameOnKillerListener(),
            TamingListener(),
        )
    }
}
