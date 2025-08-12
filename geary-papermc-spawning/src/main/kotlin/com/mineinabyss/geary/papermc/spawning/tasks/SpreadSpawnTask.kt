package com.mineinabyss.geary.papermc.spawning.tasks

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.World

class SpreadSpawnTask(world: World, configs: SpreadSpawnSectionsConfig, val spreadSpawner: SpreadSpawner) {

    val job = gearyPaper.plugin.launch(gearyPaper.plugin.asyncDispatcher) {
        while (true) {
            runCatching {
                spreadSpawner.clearOldEntries(world, configs.clearSpawnsOlderThan)
                spreadSpawner.spawnSpreadEntities()
            }.onFailure {
                gearyPaper.logger.e { it.stackTraceToString() }
            }
            delay(configs.spawnDelay.ticks)
        }
    }

    fun cancel() {
        job.cancel()
    }
}
