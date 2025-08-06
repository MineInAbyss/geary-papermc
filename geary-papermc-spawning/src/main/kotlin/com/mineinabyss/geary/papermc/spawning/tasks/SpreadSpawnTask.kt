package com.mineinabyss.geary.papermc.spawning.tasks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import kotlinx.coroutines.delay
import kotlin.time.Duration

class SpreadSpawnTask(
    val spreadSpawner: SpreadSpawner,
    val delay: Duration,
) {
    val job = gearyPaper.plugin.launch {
        while (true) {
            runCatching {
                spreadSpawner.spawnSpreadEntities()
            }.onFailure {
                gearyPaper.logger.e { it.stackTraceToString() }
            }
            delay(delay)
        }
    }

    fun cancel() {
        job.cancel()
    }
}
