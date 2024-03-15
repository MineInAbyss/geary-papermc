package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.datatypes.maps.SynchronizedTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModuleProvider
import com.mineinabyss.geary.papermc.Catching.Companion.asyncCheck
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.injectLogger
import com.mineinabyss.idofront.time.ticks

class ConfigBased(
    config: GearyPaperConfig,
    plugin: GearyPlugin,
) {
    val logger = ComponentLogger.forPlugin(plugin, minSeverity = config.logLevel)
}

class PaperEngineModule(
    val plugin: GearyPlugin
) : ArchetypeEngineModule(tickDuration = 1.ticks) {
    private var configBased = updateToMatch(gearyPaper.config)

    override val engine: ArchetypeEngine = PaperMCEngine()
    override val logger get() = configBased.logger

    override val entityProvider: EntityByArchetypeProvider
        get() {
            asyncCheck(gearyPaper.config.catch.asyncWrite, "Async entityProvider access!")
            return super.entityProvider
        }
    override val read: ArchetypeReadOperations
        get() {
            asyncCheck(gearyPaper.config.catch.asyncRead, "Async entity read!")
            return super.read
        }
    override val write: ArchetypeMutateOperations
        get() {
            asyncCheck(gearyPaper.config.catch.asyncWrite, "Async entity write!")
            return super.write
        }

    private val syncTypeMap = SynchronizedTypeMap(super.records)

    override val records: TypeMap
        get() {
            asyncCheck(gearyPaper.config.catch.asyncRecordsAccess, "Async records access!")
            return syncTypeMap
        }


    override val archetypeProvider: ArchetypeProvider
        get() {
            asyncCheck(gearyPaper.config.catch.asyncArchetypeProviderAccess, "Async archetype provider access!")
            return super.archetypeProvider
        }

    fun updateToMatch(config: GearyPaperConfig): ConfigBased {
        val configBased = ConfigBased(config, plugin)
        this.configBased = configBased
        plugin.injectLogger(logger)
        return configBased
    }

    companion object : GearyModuleProvider<PaperEngineModule> {
        override fun start(module: PaperEngineModule) {
            DI.add<PaperEngineModule>(module)
        }

        override fun init(module: PaperEngineModule) {
            ArchetypeEngineModule.init(module)
        }
    }
}
