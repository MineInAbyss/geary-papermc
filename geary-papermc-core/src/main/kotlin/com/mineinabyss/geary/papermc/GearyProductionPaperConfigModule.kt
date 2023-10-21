package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.config
import org.bukkit.plugin.java.JavaPlugin

class GearyProductionPaperConfigModule(
    override val plugin: JavaPlugin,
) : GearyPaperConfigModule {
    override val configHolder = config<GearyPaperConfig>("config", plugin.dataFolder.toPath(), GearyPaperConfig(), mergeUpdates = true)

    override val config: GearyPaperConfig by configHolder
}
