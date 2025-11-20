package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addons.dsl.AddonBuilder
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureBuilder
import com.mineinabyss.idofront.features.FeatureDSLMarker

@FeatureDSLMarker
fun FeatureBuilder.configureGeary(load: AddonBuilder.() -> Unit) {
//    load(get<Geary>())
}

fun <T : Any> Geary.getAddon(feature: Feature<T>): T {
    TODO()
}