package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.GearyModuleProvider
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay

class PaperEngineModule(val plugin: GearyPlugin) :
    ArchetypeEngineModule(tickDuration = 1.ticks) {
    override val engine: ArchetypeEngine = PaperMCEngine()
    override val logger = Logger(StaticConfig(logWriterList = listOf(PaperWriter(plugin))))

    companion object: GearyModuleProvider<PaperEngineModule> {
        override fun start(module: PaperEngineModule) {
            module.plugin.launch {
                delay(1.ticks) // Waits until first tick has complete (all plugins loaded)
                ArchetypeEngineModule.start(module)
            }
        }

        override fun init(module: PaperEngineModule) {
            ArchetypeEngineModule.init(module)
        }
    }
}
