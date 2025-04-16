package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.modules.UninitializedGearyModule
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.java.JavaPlugin

val gearyPaper: GearyPaperModule by DI.observe()

interface GearyPaperModule {
    val plugin: GearyPlugin
    val configHolder: IdofrontConfig<GearyPaperConfig>
    val config: GearyPaperConfig
    val logger: ComponentLogger
    val features: Features
    val gearyModule: UninitializedGearyModule
    val worldManager: WorldManager
}

inline fun GearyPaperModule.configure(configure: GearySetup.() -> Unit) = gearyModule.configure(configure)
