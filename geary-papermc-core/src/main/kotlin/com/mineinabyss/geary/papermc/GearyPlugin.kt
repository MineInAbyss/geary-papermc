package com.mineinabyss.geary.papermc

import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

abstract class GearyPlugin: JavaPlugin(), KoinComponent {
    abstract val application: KoinApplication

    override fun getKoin(): Koin = application.koin
}
