package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addons.dsl.AddonSetup
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.idofront.di.DIContext

class GearyPaper(
    val plugin: GearyPlugin,
    module: GearyModule,
    context: DIContext,
) : Geary(module, context) {
}

inline fun AddonSetup<*>.onPluginEnable(crossinline run: GearyPaper.() -> Unit) {
    onStart {
        TODO()
//        run(GearyPaper(gearyPaper.plugin as GearyPlugin, module, context))
    }
}
