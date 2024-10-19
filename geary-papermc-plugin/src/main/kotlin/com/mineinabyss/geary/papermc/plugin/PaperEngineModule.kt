package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.datatypes.maps.SynchronizedArrayTypeMap
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.Catching.Companion.asyncCheck
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.messaging.injectedLogger
import com.mineinabyss.idofront.time.ticks
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

fun GearyPlugin.PaperEngineModule(): GearyModule {
    val engine = ArchetypeEngineModule(useSynchronized = true)

    return GearyModule(
        module {
            includes(engine.module)
            single<GearyPlugin> { this@PaperEngineModule }
            singleOf(::PaperMCEngine) withOptions { bind<Engine>(); bind<ArchetypeEngine>() }
            single<Logger> { this@PaperEngineModule.injectedLogger() }
        },
        engine.properties
    )
}

//class PaperEngineModule(
//    val plugin: GearyPlugin,
//) : ArchetypeEngineModule(tickDuration = 1.ticks) {
//    override val entityProvider: EntityByArchetypeProvider
//        get() {
//            asyncCheck(gearyPaper.config.catch.asyncWrite, "Async entityProvider access!")
//            return super.entityProvider
//        }
//    override val read: ArchetypeReadOperations
//        get() {
//            asyncCheck(gearyPaper.config.catch.asyncRead, "Async entity read!")
//            return super.read
//        }
//    override val write: ArchetypeMutateOperations
//        get() {
//            asyncCheck(gearyPaper.config.catch.asyncWrite, "Async entity write!")
//            return super.write
//        }

//    private val syncTypeMap = SynchronizedArrayTypeMap()
//
//    override val records: SynchronizedArrayTypeMap
//        get() {
//            asyncCheck(gearyPaper.config.catch.asyncRecordsAccess, "Async records access!")
//            return syncTypeMap
//        }
//
//
//    override val archetypeProvider: ArchetypeProvider
//        get() {
//            asyncCheck(gearyPaper.config.catch.asyncArchetypeProviderAccess, "Async archetype provider access!")
//            return super.archetypeProvider
//        }
//
//    companion object : GearyModuleProvider<PaperEngineModule> {
//        override fun start(module: PaperEngineModule) {
//            ArchetypeEngineModule.start(module)
//        }
//
//        override fun init(module: PaperEngineModule) {
//            ArchetypeEngineModule.init(module)
//        }
//    }
//}
//
