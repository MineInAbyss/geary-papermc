package com.mineinabyss.geary.papermc.spawning.tasks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import me.dvyy.sqlite.Database
import org.bukkit.World
import kotlin.time.Duration.Companion.seconds

class SpreadSpawnTask( val spreadSpawner: SpreadSpawner) {

    val job = gearyPaper.plugin.launch {
        while (true) {
            runCatching {
                spreadSpawner.spawnSpreadEntities()
            }.onFailure {
                gearyPaper.logger.e { it.stackTraceToString() }
            }
            delay(spreadSpawner.configs.spawn_delay.ticks)
        }
    }

    fun cancel() {
        job.cancel()
    }
}