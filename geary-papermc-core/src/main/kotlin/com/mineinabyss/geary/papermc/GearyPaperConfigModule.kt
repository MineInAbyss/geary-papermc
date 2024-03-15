package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.java.JavaPlugin

val gearyPaper: GearyPaperConfigModule by DI.observe()

interface GearyPaperConfigModule {
    val plugin: JavaPlugin
    val configHolder: IdofrontConfig<GearyPaperConfig>
    val config: GearyPaperConfig
    val logger: ComponentLogger
}
