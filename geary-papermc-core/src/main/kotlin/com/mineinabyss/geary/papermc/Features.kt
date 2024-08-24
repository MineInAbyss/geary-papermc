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
    val enabled = mutableListOf<Feature>()
    private var isFirstEnable = true

    fun enableAll() {
        features.forEach {
            enable(it)
        }
        isFirstEnable = false
    }

    fun enable(builder: FeatureBuilder): Result<Feature> {
        val logger = plugin.injectedLogger()
        val context = FeatureContext(plugin, logger, isFirstEnable)
        val feature = builder(context)
        featuresByClass[feature::class] = builder
        return if (feature.defaultCanEnable()) {
            runCatching {
                feature.defaultEnable()
                enabled.add(feature)
                feature
            }.onFailure { it.printStackTrace() }
        } else Result.failure(IllegalStateException("Feature ${feature::class.simpleName} could not be enabled"))
    }

    fun disableAll() {
        enabled.forEach(Feature::defaultDisable)
        enabled.clear()
    }

    fun reloadAll() {
        disableAll()
        enableAll()
    }

    inline fun <reified T : Feature> getOrNull() = enabled.firstOrNull { it is T } as? T

    inline fun <reified T : Feature> reload(notify: CommandSender? = null) {
        val feature = getOrNull<T>()
        feature?.defaultDisable() ?: error("Feature ${T::class.simpleName} has never been loaded!")
        enabled.remove(feature)
        enable(featuresByClass[T::class] ?: error("Feature ${T::class.simpleName} has never been loaded!"))
            .onSuccess { notify?.success("Reloaded ${T::class.simpleName}") }
            .onFailure { notify?.error("Failed to reload ${T::class.simpleName}\n${it.stackTraceToString()}") }

    }
}
