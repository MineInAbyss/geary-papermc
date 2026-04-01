package com.mineinabyss.geary.papermc

//fun FeatureBuilder.configureGeary(load: AddonBuilder.(di: FeatureDI) -> Unit) {
//    onEnable {
////        val featureScope = this.scope
//        val geary = get<Geary>()
//        val addon = createAddon<Any>(this@configureGeary.name + "-geary", type = this@configureGeary.type as KClass<Any>) {
////            onEnable {
////                val gearyScope = this.scope
////                gearyScope.linkTo(featureScope)
////            }
//            load(this@onEnable)
//        }
//
//        geary.addons.install(addon)
//        addCloseable { geary.addons.uninstall(addon) }
//    }
//}

//fun <T : Any> Geary.getAddon(feature: Feature<T>): T {
//    return addons.getScope(feature.name + "-geary").scope.get(feature.type)
//}