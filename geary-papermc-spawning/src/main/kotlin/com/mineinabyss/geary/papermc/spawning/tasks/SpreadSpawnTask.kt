package com.mineinabyss.geary.papermc.spawning.tasks

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadEntityTracker
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.idofront.messaging.ComponentLogger
import kotlinx.coroutines.withContext
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
    tracker: SpreadEntityTracker,
) {
    private var lastCleanup = TimeSource.Monotonic.markNow() - 1.minutes

    val job = plugin.launchTickRepeating(configs.spawnDelay) {
        runCatching {
            if (lastCleanup.elapsedNow() >= 1.minutes) {
                val liveIds = withContext(gearyPaper.minecraftDispatcher) { tracker.liveIds() }
                spreadSpawns.deleteSpawnsOlderThan(world, configs.clearSpawnsOlderThan, liveIds)
                lastCleanup = TimeSource.Monotonic.markNow()
            }
            spreadSpawner.spawnSpreadEntities()
        }.onFailure {
            logger.e { it.stackTraceToString() }
        }
    }
}
