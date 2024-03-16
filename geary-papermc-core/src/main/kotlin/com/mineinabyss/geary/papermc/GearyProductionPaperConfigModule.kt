package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.injectLogger
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.plugin.dataPath
import org.bukkit.plugin.java.JavaPlugin

class GearyProductionPaperConfigModule(
    override val plugin: JavaPlugin,
) : GearyPaperConfigModule {
    override val configHolder = config(
        "config", plugin.dataPath, GearyPaperConfig(),
        onLoad = {
            plugin.injectLogger(ComponentLogger.forPlugin(plugin, minSeverity = it.logLevel))
        }
    )

    override val config: GearyPaperConfig by configHolder

    override val logger by plugin.observeLogger()
}
