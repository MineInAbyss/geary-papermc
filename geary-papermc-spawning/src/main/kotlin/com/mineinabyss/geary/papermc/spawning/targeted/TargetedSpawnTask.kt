package com.mineinabyss.geary.papermc.spawning.targeted

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import kotlinx.coroutines.delay
import me.dvyy.sqlite.Database
import kotlin.time.Duration.Companion.seconds

class TargetedSpawnTask(db: Database) {
    val tgs = TargetedSpawner()
    val job = gearyPaper.plugin.launch {
        while (true) {
            runCatching {
                generateSpawnLocation(db, tgs)
            }.onFailure {
                gearyPaper.logger.e { it.stackTraceToString() }
            }
            delay(2.seconds)
        }
    }
    fun cancel() {
        job.cancel()
    }
}