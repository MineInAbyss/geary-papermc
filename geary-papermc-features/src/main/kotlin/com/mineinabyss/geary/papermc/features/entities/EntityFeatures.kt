package com.mineinabyss.geary.papermc.features.entities

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.entities.bucketable.BucketableListener
import com.mineinabyss.geary.papermc.features.entities.commands.mobs
import com.mineinabyss.geary.papermc.features.entities.displayname.ShowDisplayNameOnKillerListener
import com.mineinabyss.geary.papermc.features.entities.prevent.PreventEventsFeature
import com.mineinabyss.geary.papermc.features.entities.sounds.AmbientSoundsFeature
import com.mineinabyss.geary.papermc.features.entities.taming.TamingListener
import com.mineinabyss.idofront.features.feature

val EntityFeatures = feature("entities") {
    dependsOn {
        condition { get<GearyPaperConfig>().minecraftFeatures }
    }

    install(
        AmbientSoundsFeature,
        PreventEventsFeature,
    )

    onEnable {
        listeners(
            BucketableListener(),
            ShowDisplayNameOnKillerListener(),
            TamingListener(),
        )
    }

    mainCommand {
        mobs()
    }
}
