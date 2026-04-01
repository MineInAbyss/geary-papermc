package com.mineinabyss.geary.papermc.plugin

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.features.get
import com.mineinabyss.geary.helpers.async.IgnoringAsyncCatcher
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.*
import kotlinx.coroutines.CoroutineName
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private fun GearyPlugin.paperModule() = DI.Module("geary-papermc") {
    bindSingleton<GearyPlugin> { this@paperModule }
    bindSingleton {
        PaperMCEngine(get(), get(), instance("engineThread"))
    }
}

private fun chooseCatcher(catchType: CatchType) = when (catchType) {
    CatchType.IGNORE -> IgnoringAsyncCatcher()
    CatchType.WARN -> PaperWarningAsyncCatcher()
    CatchType.ERROR -> PaperAsyncCatcher()
}

fun GearyPlugin.PaperEngineModule(config: GearyPaperConfig): GearyModule {
    val engine = ArchetypeEngineModule(
        logger = null,
        useSynchronized = true,
        engineThread = { minecraftDispatcher + CoroutineName("Geary Engine") },
    )
    return GearyModule(
        DI.Module("geary-papermc") {
            bindSingleton("asyncCatcher.write") { chooseCatcher(config.catch.asyncWrite) }
            importAll(paperModule(), engine.module)
        },
    )
}
