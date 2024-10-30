package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addons.dsl.AddonSetup
import com.mineinabyss.geary.modules.Geary

class GearyPaper(
    val plugin: GearyPlugin,
    world: Geary,
) : Geary by world

inline fun AddonSetup<*>.onPluginEnable(crossinline run: GearyPaper.() -> Unit) {
    onStart {
        GearyPaper(application.koin.get<GearyPlugin>(), this).run()
    }
}
