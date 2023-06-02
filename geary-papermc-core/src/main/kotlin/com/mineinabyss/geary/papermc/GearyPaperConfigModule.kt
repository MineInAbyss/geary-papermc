package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.di.DI
import org.bukkit.plugin.java.JavaPlugin

val gearyPaper: GearyPaperConfigModule by DI.observe()

interface GearyPaperConfigModule {
    val plugin: JavaPlugin
    val config: GearyPaperConfig
}
