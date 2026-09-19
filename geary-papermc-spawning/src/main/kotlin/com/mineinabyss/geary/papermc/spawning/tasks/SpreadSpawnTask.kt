package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.World
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

class SpreadSpawnTask(
    world: World,
    configs: SpreadEntityTypesConfig,
    plugin: Plugin,
    logger: ComponentLogger,
    spreadSpawner: SpreadSpawner,
    spreadSpawns: SpreadSpawnRepository,
) {
    private var lastCleanup = TimeSource.Monotonic.markNow() - 1.minutes

    val job = plugin.launchTickRepeating(configs.spawnDelay) {
        runCatching {
            if (lastCleanup.elapsedNow() >= 1.minutes) {
                spreadSpawns.deleteSpawnsOlderThan(world, configs.clearSpawnsOlderThan)
                lastCleanup = TimeSource.Monotonic.markNow()
            }
            spreadSpawner.spawnSpreadEntities()
        }.onFailure {
            logger.e { it.stackTraceToString() }
        }
    }
}
