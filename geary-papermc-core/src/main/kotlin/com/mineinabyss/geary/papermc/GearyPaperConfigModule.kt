package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.di.DI
import org.bukkit.plugin.Plugin

val gearyPaper: GearyPaperConfigModule by DI.observe()

interface GearyPaperConfigModule {
    val plugin: Plugin
    val config: GearyPaperConfig
}
