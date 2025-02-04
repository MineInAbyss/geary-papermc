package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.plugin.listeners
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

abstract class Feature(context: FeatureContext) {
    open val name = this::class.simpleName
    val plugin = context.plugin
    open val logger = context.logger
    val listeners = mutableListOf<Listener>()
    val tasks = mutableListOf<Job>()
    private var pluginDeps = listOf<String>()

    open val subFeatures = Features(plugin)

    fun pluginDeps(vararg plugins: String) {
        pluginDeps = plugins.toList()
    }

    open fun canLoad(): Boolean = true

    open fun canEnable(): Boolean = true

    open fun load() {}

    open fun enable() {}

    open fun disable() {}

    fun defaultCanLoad(): Boolean {
        val loadedPlugins = Bukkit.getPluginManager().plugins.map { it.name }.toSet()
        val unmet = pluginDeps.filter { it !in loadedPlugins }
        if (unmet.isNotEmpty()) {
            logger.w { "Plugin load dependencies not met for $name: $unmet" }
            return false
        }
        return canLoad()
    }

    fun defaultCanEnable(): Boolean {
        val unmet = pluginDeps.filter { !plugin.server.pluginManager.isPluginEnabled(it) }
        if (unmet.isNotEmpty()) {
            logger.w { "Plugin enable dependencies not met for $name: $unmet" }
            return false
        }
        return canEnable()
    }

    fun defaultDisable() {
        logger.i { "Disabling ${this::class.simpleName}" }
        disable()
        subFeatures.disableAll()
        listeners.forEach {
            HandlerList.unregisterAll(it)
        }
        listeners.clear()

        tasks.forEach {
            it.cancel()
        }
        tasks.clear()
    }

    fun defaultLoad() = runCatching {
        subFeatures.loadAll()
        load()
    }.onSuccess {
        logger.s("Loaded ${this::class.simpleName}")
    }.onFailure {
        logger.f("Failed to load ${this::class.simpleName}")
    }

    fun defaultEnable() = runCatching {
        subFeatures.enableAll()
        enable()
    }.onSuccess {
        logger.s("Enabled ${this::class.simpleName}")
    }.onFailure {
        logger.f("Failed to enable ${this::class.simpleName}")
    }

    fun listeners(vararg listeners: Listener) {
        this.listeners.addAll(listeners)
        plugin.listeners(*listeners)
    }

    fun task(task: Job) {
        tasks.add(task)
    }

    fun subFeatures(vararg features: FeatureBuilder) = Features(plugin, *features)
}
