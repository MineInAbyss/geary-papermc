package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addons.dsl.AddonBuilder
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureBuilder
import com.mineinabyss.idofront.features.FeatureDSLMarker
import kotlin.reflect.KClass

@FeatureDSLMarker
fun FeatureBuilder.configureGeary(load: AddonBuilder.() -> Unit) {
    onEnable {
        val featureScope = this.scope
        val geary = get<Geary>()
        val addon = createAddon<Any>(this@configureGeary.name + "-geary", type = this@configureGeary.type as KClass<Any>) {
            onEnable {
                val gearyScope = this.scope
                gearyScope.linkTo(featureScope)
            }
            load()
        }

        geary.addons.install(addon)

        addCloseables({
            geary.addons.uninstall(addon)
        })
    }
}

fun <T : Any> Geary.getAddon(feature: Feature<T>): T {
    return addons.getScope(feature.name + "-geary").scope.get(feature.type)
}