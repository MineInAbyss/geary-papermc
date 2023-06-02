package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.config
import org.bukkit.plugin.java.JavaPlugin

class GearyProductionPaperConfigModule(
    override val plugin: JavaPlugin,
): GearyPaperConfigModule {
    override val config by config<GearyPaperConfig>("config") {
        plugin.fromPluginPath()
        mergeUpdates = true
    }
}
