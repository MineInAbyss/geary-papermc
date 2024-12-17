package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.time.ticks
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

class PaperMCEngine(
    logger: Logger,
    pipeline: Pipeline,
    coroutineContext: () -> CoroutineContext,
) : ArchetypeEngine(pipeline, logger, tickDuration = 1.ticks, coroutineContext) {
    private val plugin get() = gearyPaper.plugin

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            tick(Bukkit.getServer().currentTick.toLong())
        }, 0, 1)
    }
}
