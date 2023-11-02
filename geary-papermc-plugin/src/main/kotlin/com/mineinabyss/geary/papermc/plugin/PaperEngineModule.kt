package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModuleProvider
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.time.ticks

class PaperEngineModule(val plugin: GearyPlugin) :
    ArchetypeEngineModule(tickDuration = 1.ticks) {
    override val engine: ArchetypeEngine = PaperMCEngine()
    override val logger = Logger(StaticConfig(logWriterList = listOf(PaperWriter(plugin)), minSeverity = gearyPaper.config.logLevel))

    companion object: GearyModuleProvider<PaperEngineModule> {
        override fun start(module: PaperEngineModule) {
            DI.add<PaperEngineModule>(module)
        }

        override fun init(module: PaperEngineModule) {
            ArchetypeEngineModule.init(module)
        }
    }
}
