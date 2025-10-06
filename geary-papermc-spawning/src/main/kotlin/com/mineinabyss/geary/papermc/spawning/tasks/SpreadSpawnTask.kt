package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import org.bukkit.World

class SpreadSpawnTask(world: World, configs: SpreadSpawnSectionsConfig, val spreadSpawner: SpreadSpawner) {
    val job = gearyPaper.plugin.launchTickRepeating(configs.spawnDelay) {
        runCatching {
            spreadSpawner.clearOldEntries(world, configs.clearSpawnsOlderThan)
            spreadSpawner.spawnSpreadEntities()
        }.onFailure {
            gearyPaper.logger.e { it.stackTraceToString() }
        }
    }

    fun cancel() {
        job.cancel()
    }
}
