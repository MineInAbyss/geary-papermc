package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.World
import org.bukkit.plugin.Plugin

class SpreadSpawnTask(
    world: World,
    configs: SpreadEntityTypesConfig,
    plugin: Plugin,
    logger: ComponentLogger,
    spreadSpawner: SpreadSpawner,
    spreadSpawns: SpreadSpawnRepository,
) {
    val job = plugin.launchTickRepeating(configs.spawnDelay) {
        runCatching {
            spreadSpawns.deleteSpawnsOlderThan(world, configs.clearSpawnsOlderThan)
            spreadSpawner.spawnSpreadEntities()
        }.onFailure {
            logger.e { it.stackTraceToString() }
        }
    }
}
