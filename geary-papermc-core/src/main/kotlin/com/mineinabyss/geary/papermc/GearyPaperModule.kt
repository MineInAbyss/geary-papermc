package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.FeatureManager
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.java.JavaPlugin

@Deprecated("Inject as features via Koin instead")
val gearyPaper: GearyPaperModule by DI.observe()

interface GearyPaperModule {
    val plugin: JavaPlugin
    val config: GearyPaperConfig
    val logger: ComponentLogger
    val features: FeatureManager
    val worldManager: WorldManager
}

fun GearyPaperModule.configure(configure: GearySetup.() -> Unit) = worldManager.configure(configure)
