package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModuleProvider
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.time.ticks
import org.spigotmc.AsyncCatcher

class PaperEngineModule(
    val plugin: GearyPlugin
) :
    ArchetypeEngineModule(tickDuration = 1.ticks) {
    override val engine: ArchetypeEngine = PaperMCEngine()
    override val logger =
        Logger(StaticConfig(logWriterList = listOf(PaperWriter(plugin)), minSeverity = gearyPaper.config.logLevel))

    override val read: ArchetypeReadOperations
        get() {
            if (gearyPaper.config.catchAsyncRead)
                AsyncCatcher.catchOp("Async entity read!")
            return super.read
        }
    override val write: ArchetypeMutateOperations
        get() {
            if (gearyPaper.config.catchAsyncWrite)
                AsyncCatcher.catchOp("Async entity write!")
            return super.write
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
