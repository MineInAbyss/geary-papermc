package com.mineinabyss.geary.papermc

import com.mineinabyss.dependencies.DIContext
import com.mineinabyss.dependencies.DIScope
import com.mineinabyss.dependencies.MutableDI
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.Plugin

interface GearyPlugin : Plugin {
    val worldManager: WorldManager
    val config: GearyPaperConfig
    val logger: ComponentLogger
    val features: DIScope

    /**
     * Creates a new [WorldScoped] instance, calling [builder] on it for every Geary world instance.
     *
     * Currently, this is called once for a global Geary world, but may change in the future
     * with servers like Folia, where different regions may have separately ticking worlds.
     *
     * @return An [AutoCloseable], closing it will unload the module from all Geary world instances.
     */
    fun configure(builder: WorldScoped.() -> Unit): AutoCloseable

    fun forEachWorld(builder: Geary.() -> Unit)

    companion object {
        var instance: GearyPlugin? = null
    }
}

inline fun MutableDI.gearyWorld(crossinline builder: WorldScoped.() -> Unit) {
    addCloseable(gearyPaper.configure {
        val world = world
        builder(
            object : WorldScoped {
                override val world: Geary = world
                override val di: DIContext = this@gearyWorld.di
            })
    })
}

val gearyPaper: GearyPlugin get() = GearyPlugin.instance ?: error("Geary plugin not loaded!")
