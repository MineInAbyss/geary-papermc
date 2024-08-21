package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.plugin.listeners
import kotlinx.coroutines.Job
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

abstract class Feature(context: FeatureContext) {
    val plugin = context.plugin
    val logger = context.logger
    val listeners = mutableListOf<Listener>()
    val tasks = mutableListOf<Job>()

    open fun canEnable(): Boolean = true

    open fun enable() {}

    open fun disable() {}

    fun defaultDisable() {
        logger.i { "Disabling ${this::class.simpleName}" }
        disable()
        listeners.forEach {
            HandlerList.unregisterAll(it)
        }
        listeners.clear()

        tasks.forEach {
            it.cancel()
        }
        tasks.clear()
    }

    fun defaultEnable() {
        logger.i { "Enabling ${this::class.simpleName}" }
        enable()
    }

    fun listeners(vararg listeners: Listener) {
        this.listeners.addAll(listeners)
        plugin.listeners(*listeners)
    }

    fun task(task: Job) {
        tasks.add(task)
    }
}
