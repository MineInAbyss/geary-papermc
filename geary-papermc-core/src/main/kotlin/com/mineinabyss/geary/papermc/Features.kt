package com.mineinabyss.geary.papermc

import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.injectedLogger
import com.mineinabyss.idofront.messaging.success
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

typealias FeatureBuilder = (FeatureContext) -> Feature

class Features(
    val plugin: Plugin,
    vararg val features: FeatureBuilder,
) {
    val featuresByClass = mutableMapOf<KClass<*>, FeatureBuilder>()
    val loaded = mutableListOf<Feature>()
    val enabled = mutableListOf<Feature>()
    val context get() = FeatureContext(plugin, plugin.injectedLogger(), isFirstEnable)
    private var isFirstEnable = true

    fun loadAll(isFirstLoad: Boolean = true) {
        features.forEach { load(it, isFirstLoad) }
    }

    fun enableAll() {
        loaded.forEach(::enable)
        isFirstEnable = false
    }

    fun load(builder: FeatureBuilder, isFirstLoad: Boolean): Result<Feature> = runCatching {
        val feature = builder(context)
        featuresByClass[feature::class] = builder
        if (!feature.canLoad()) return Result.failure(IllegalStateException("Feature ${feature.name} could not be loaded"))
        return feature.defaultLoad(isFirstLoad)
            .onSuccess { loaded.add(feature) }
            .onFailure { it.printStackTrace() }
            .map { feature }
    }.onFailure {
        context.logger.e { "Failed to create feature from constructor: $builder" }
        it.printStackTrace()
    }

    fun enable(feature: Feature): Result<Feature> {
        if (!feature.defaultCanEnable()) return Result.failure(IllegalStateException("Feature ${feature::class.simpleName} could not be enabled"))
        enabled.add(feature)
        return feature.defaultEnable()
            .onFailure {
                enabled.remove(feature)
                it.printStackTrace()
            }
            .map { feature }
    }

    fun disableAll() {
        enabled.forEach(Feature::defaultDisable)
        enabled.clear()
        loaded.clear()
    }

    fun reloadAll() {
        disableAll()
        loadAll(isFirstLoad = false)
        enableAll()
    }

    inline fun <reified T : Feature> getOrNull() = enabled.firstOrNull { it is T } as? T

    inline fun <reified T : Feature> get() = getOrNull<T>() ?: error("Feature ${T::class.simpleName} is not enabled!")

    inline fun <reified T : Feature> reload(notify: CommandSender? = null) {
        val builder = featuresByClass[T::class] ?: error("Feature ${T::class.simpleName} has never been loaded!")
        val feature = getOrNull<T>()!!
        feature.defaultDisable()
        enabled.remove(feature)
        load(builder, isFirstLoad = false)
            .map { enable(it) }
            .onSuccess { notify?.success("Reloaded ${T::class.simpleName}") }
            .onFailure { notify?.error("Failed to reload ${T::class.simpleName}\n${it.stackTraceToString()}") }

    }
}
