package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.launchTickRepeating
import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.time.ticks
import org.bukkit.Bukkit
import org.bukkit.GameMode.SPECTATOR
import kotlin.time.Duration

class SpawnTask(
    config: SpawnConfig,
    private val locationChooser: SpawnLocationChooser,
    private val mobSpawner: MobSpawner,
) {
    private val runTimes: Map<SpawnPosition, Duration> = config.runTimes
    private val spawnAttempts: Int = config.maxSpawnAttemptsPerPlayer

    val job = gearyPaper.plugin.launchTickRepeating(config.taskDelay) {
        runCatching { run() }.onFailure {
            gearyPaper.logger.d { it.stackTraceToString() }
        }
    }

    fun run() {
        val currTick = Bukkit.getCurrentTick()
        val allowedSpawnPositions: List<SpawnPosition> = SpawnPosition.entries
            .filter { currTick % runTimes.getOrDefault(it, 1.ticks).inWholeTicks == 0L }
            .takeUnless { it.isEmpty() } ?: return
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { !it.isDead && it.gameMode != SPECTATOR }

        onlinePlayers.forEach { player ->
            val attemptedPositions = allowedSpawnPositions.toMutableSet()
            repeat(spawnAttempts) {
                if (attemptedPositions.isEmpty()) return@forEach
                val spawnLoc = locationChooser.chooseSpawnLocationNear(onlinePlayers, player.location) ?: return@repeat
                val type = SpawnPositionReader.spawnPositionFor(spawnLoc)
                if (type in allowedSpawnPositions) {
                    attemptedPositions.remove(type)
                    mobSpawner.attemptSpawnAt(spawnLoc, type)
                }
            }
        }
    }
}
