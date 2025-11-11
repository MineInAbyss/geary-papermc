package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.helpers.async.IgnoringAsyncCatcher
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.*
import com.mineinabyss.idofront.messaging.injectedLogger
import kotlinx.coroutines.CoroutineName
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private fun GearyPlugin.paperModule() = module {
    single<Logger> { this@paperModule.injectedLogger() }
    single<GearyPlugin> { this@paperModule }
    single {
        PaperMCEngine(get(), get(), getProperty("engineThread"))
    } withOptions {
        bind<Engine>(); bind<ArchetypeEngine>()
    }
}

private fun chooseCatcher(catchType: CatchType) = when (catchType) {
    CatchType.IGNORE -> IgnoringAsyncCatcher()
    CatchType.WARN -> PaperWarningAsyncCatcher()
    CatchType.ERROR -> PaperAsyncCatcher()
}

fun GearyPlugin.PaperEngineModule(config: GearyPaperConfig): GearyModule {
    val engine = ArchetypeEngineModule(
        useSynchronized = true,
        engineThread = { minecraftDispatcher + CoroutineName("Geary Engine") },
        properties = mapOf(
            "asyncCatcher.write" to chooseCatcher(config.catch.asyncWrite),
        )
    )
    return GearyModule(
        module {
            includes(paperModule(), engine.module)
        },
        engine.properties
    )
}
