package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.idofront.messaging.injectedLogger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

fun GearyPlugin.PaperEngineModule(): GearyModule {
    val engine = ArchetypeEngineModule(useSynchronized = true)

    return GearyModule(
        module {
            includes(engine.module)
            single<GearyPlugin> { this@PaperEngineModule }
            single {
                PaperMCEngine(get(), get(), getProperty("engineThread"))
            } withOptions {
                bind<Engine>(); bind<ArchetypeEngine>()
            }
            single<Logger> { this@PaperEngineModule.injectedLogger() }
        },
        engine.properties
    )
}
