package com.mineinabyss.geary.papermc.features.entities

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.Features
import com.mineinabyss.geary.papermc.features.entities.bucketable.BucketableListener
import com.mineinabyss.geary.papermc.features.entities.displayname.ShowDisplayNameOnKillerListener
import com.mineinabyss.geary.papermc.features.entities.prevent.PreventEventsFeature
import com.mineinabyss.geary.papermc.features.entities.sounds.AmbientSoundsFeature
import com.mineinabyss.geary.papermc.features.entities.taming.TamingListener
import com.mineinabyss.geary.papermc.gearyPaper

class EntityFeatures(context: FeatureContext) : Feature(context) {
    private val subFeatures = Features(
        context.plugin,
        ::AmbientSoundsFeature,
        ::PreventEventsFeature
    )

    override fun canEnable(): Boolean = gearyPaper.config.trackEntities

    override fun enable() {
        subFeatures.enableAll()
        listeners(
            BucketableListener(),
            ShowDisplayNameOnKillerListener(),
            TamingListener(),
        )
    }

    override fun disable() {
        subFeatures.disableAll()
    }
}
