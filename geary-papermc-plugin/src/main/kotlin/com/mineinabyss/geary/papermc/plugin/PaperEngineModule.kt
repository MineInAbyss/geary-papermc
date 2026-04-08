package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.helpers.async.IgnoringAsyncCatcher
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.papermc.*
import kotlinx.coroutines.CoroutineName

private fun chooseCatcher(catchType: CatchType) = when (catchType) {
    CatchType.IGNORE -> IgnoringAsyncCatcher()
    CatchType.WARN -> PaperWarningAsyncCatcher()
    CatchType.ERROR -> PaperAsyncCatcher()
}

fun GearyPlugin.PaperEngineModule(
    logger: Logger,
    config: GearyPaperConfig,
): DI.Module {
    val engine = ArchetypeEngineModule(
        logger = logger,
        useSynchronized = true,
        engineThread = { minecraftDispatcher + CoroutineName("Geary Engine") },
    ).override {
        single("asyncCatcher.write") { chooseCatcher(config.catch.asyncWrite) }
        single<ArchetypeEngine>(ignoreOverride = true) { PaperMCEngine(get(), get(), get("engineThread")) }
    }
    return engine
}
