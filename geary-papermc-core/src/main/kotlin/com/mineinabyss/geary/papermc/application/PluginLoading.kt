package com.mineinabyss.geary.papermc.application

import com.mineinabyss.geary.addons.Application
import com.mineinabyss.geary.addons.install
import com.mineinabyss.idofront.commands.brigadier.RootIdoCommands
import com.mineinabyss.idofront.commands.brigadier.commands
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

class PluginLoading(val plugin: Plugin): Listener {
    private val onEnable = mutableListOf<Plugin.() -> Unit>()
    private val onDisable = mutableListOf<Plugin.() -> Unit>()

    fun onEnable(block: Plugin.() -> Unit) {
        onEnable.add(block)
    }

    fun onDisable(block: Plugin.() -> Unit) {
        onDisable.add(block)
    }

    inline fun commands(crossinline init: RootIdoCommands.() -> Unit) = plugin.commands(init)

    @EventHandler
    private fun PluginEnableEvent.onEnable() {
        if (this.plugin == this@PluginLoading.plugin) onEnable.forEach {
            it(plugin)
        }
    }

    @EventHandler
    private fun PluginDisableEvent.onDisable(block: Plugin.() -> Unit) {
        if (this.plugin == this@PluginLoading.plugin) onDisable.forEach {
            it(plugin)
        }
    }

    companion object Addon:
        com.mineinabyss.geary.addons.Addon<Application, PluginLoading, PluginLoading> {
        override fun install(app: Application, configure: PluginLoading.() -> Unit): PluginLoading {
            val plugin = app.di.get<Plugin>()
            return PluginLoading(plugin).apply(configure)
        }
    }
}

fun Application.plugin(configure: PluginLoading.() -> Unit) = install(PluginLoading).configure()

fun Application.onPluginEnable(configure: Plugin.() -> Unit) = install(PluginLoading).apply {
    onEnable(configure)
}
