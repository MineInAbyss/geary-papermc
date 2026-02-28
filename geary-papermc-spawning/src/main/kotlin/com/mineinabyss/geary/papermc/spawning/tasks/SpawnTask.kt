package com.mineinabyss.geary.papermc.spawning.tasks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.GameMode.SPECTATOR
import kotlin.time.Duration

class SpawnTask(
    val spawnDelay: Duration,
    val runTimes: Map<SpawnPosition, Duration>,
    val locationChooser: SpawnLocationChooser,
    val spawnAttempts: Int,
    val mobSpawner: MobSpawner,
) {
    val job = gearyPaper.plugin.launch {
        while (true) {
            runCatching {
                run()
            }.onFailure {
                gearyPaper.logger.d { it.stackTraceToString() }
            }
            delay(spawnDelay)
        }
    }

    fun run() {
        val currTick = Bukkit.getCurrentTick()
        val allowedSpawnPositions = SpawnPosition.entries
            .filter { currTick % runTimes.getOrDefault(it, 1.ticks).inWholeTicks == 0L }
            .ifEmpty { return }
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { !it.isDead && it.gameMode != SPECTATOR }

        onlinePlayers.forEach { player ->
            val attemptedPositions = allowedSpawnPositions.toMutableSet()
            repeat(spawnAttempts) {
                if (attemptedPositions.isEmpty()) return@forEach
                val spawnLoc = locationChooser.chooseSpawnLocationNear(onlinePlayers, player.location) ?: return@repeat
                val type = SpawnPositionReader.spawnPositionFor(spawnLoc)

                if (attemptedPositions.remove(type)) {
                    mobSpawner.attemptSpawnAt(spawnLoc, type)
                }
            }
        }
    }

    fun cancel() = job.cancel()
}
