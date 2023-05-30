package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.config
import org.bukkit.plugin.Plugin

class GearyProductionPaperConfigModule(
    override val plugin: Plugin,
): GearyPaperConfigModule {
    override val config by config<GearyPaperConfig>("config") {
        plugin.fromPluginPath()
        mergeUpdates = true
    }
}
