package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.features.FeatureCreate
import com.mineinabyss.idofront.features.FeatureDSLMarker

@Deprecated("Use sparingly, geary feature system will eventually be reworked to simplify this usecase")
@FeatureDSLMarker
fun FeatureCreate.configureGeary(load: Geary.() -> Unit) {
    load(get<Geary>())
}
