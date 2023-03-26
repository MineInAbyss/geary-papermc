package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI

val gearyPaper: GearyPaperModule by DI.observe()

class GearyPaperModule(
    val plugin: GearyPlugin,
) {
    val config by config<GearyPaperConfig>("config") {
        plugin.fromPluginPath()
        mergeUpdates = true
    }
}
