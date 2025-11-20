package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.engine.Pipeline
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.time.ticks
import kotlin.coroutines.CoroutineContext

class PaperMCEngine(
    logger: Logger,
    pipeline: Pipeline,
    coroutineContext: () -> CoroutineContext,
) : ArchetypeEngine(pipeline, logger, tickDuration = 1.ticks, coroutineContext) {
    private val plugin get() = gearyPaper.plugin
}
