package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.addons.ApplicationFactory
import com.mineinabyss.geary.addons.dependencies
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.modules.ArchetypeEngineConfig
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.papermc.Catching.Companion.asyncCheck
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.di.DIContext
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.time.ticks

val gearyPaper: PaperEngineModule by DI.observe()

// Don't like the confusing name with GearyPaperConfig, this one's for configuring the engine, the other is mor eplugin specific settings
class PaperEngineConfig(
    var plugin: GearyPlugin? = null,
    val archetypeConfig: ArchetypeEngineConfig = ArchetypeEngineConfig(
        tickDuration = 1.ticks,
    ),
    var configHolder: IdofrontConfig<GearyPluginConfig>? = null,
)

class PaperEngineModule(
    private val engineConfig: PaperEngineConfig,
    val plugin: GearyPlugin,
) : ArchetypeEngineModule(engineConfig.archetypeConfig) {
    override val di: DIContext = DI.scoped(plugin::class)
    override val engine: ArchetypeEngine = PaperMCEngine()
    override val logger by plugin.observeLogger()
    val config: GearyPluginConfig by engineConfig.configHolder ?: error("Config holder not set in PaperEngineConfig")

    fun reloadConfig() = engineConfig.configHolder?.reload()

    override val entityProvider: EntityByArchetypeProvider
        get() {
            asyncCheck(config.catch.asyncWrite, "Async entityProvider access!")
            return super.entityProvider
        }
    override val read: ArchetypeReadOperations
        get() {
            asyncCheck(config.catch.asyncRead, "Async entity read!")
            return super.read
        }
    override val write: ArchetypeMutateOperations
        get() {
            asyncCheck(config.catch.asyncWrite, "Async entity write!")
            return super.write
        }

    private val syncTypeMap = SynchronizedArrayTypeMap()

    override val records: SynchronizedArrayTypeMap
        get() {
            asyncCheck(config.catch.asyncRecordsAccess, "Async records access!")
            return syncTypeMap
        }


    override val archetypeProvider: ArchetypeProvider
        get() {
            asyncCheck(config.catch.asyncArchetypeProviderAccess, "Async archetype provider access!")
            return super.archetypeProvider
        }

    companion object : ApplicationFactory<PaperEngineModule, PaperEngineConfig> {
        override fun create(configure: PaperEngineConfig.() -> Unit): PaperEngineModule {
            val config = PaperEngineConfig().apply(configure)
            val plugin = config.plugin ?: error("Plugin not set in PaperEngineConfig")
            return PaperEngineModule(config, plugin).apply {
                ArchetypeEngineModule.init(this)
                dependencies {
                    add<PaperEngineModule>(this@apply)
                }
            }
        }
    }
}
