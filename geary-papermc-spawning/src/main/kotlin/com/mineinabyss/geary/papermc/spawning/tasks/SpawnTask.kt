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
    val runTimes: Map<SpawnPosition, Duration>,
    val locationChooser: SpawnLocationChooser,
    val spawnPositionReader: SpawnPositionReader,
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
            delay(1.ticks)
        }
    }

    fun run() {
        val currTick = Bukkit.getCurrentTick()
        val runForPositions: MutableSet<SpawnPosition> = SpawnPosition.entries
            .filter { currTick % runTimes.getOrDefault(it, 1.ticks).inWholeTicks == 0L }
            .toMutableSet()
        if (runForPositions.isEmpty()) return
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { !it.isDead && it.gameMode != SPECTATOR }
        if (onlinePlayers.isEmpty()) return

        onlinePlayers.forEach { player ->
            repeat(spawnAttempts) {
                val spawnLoc = locationChooser.chooseSpawnLocationNear(onlinePlayers, player.location) ?: return@repeat
                val type = spawnPositionReader.spawnPositionFor(spawnLoc)
                if (type in runForPositions) {
                    runForPositions.remove(type)
                    mobSpawner.attemptSpawnAt(spawnLoc, type)
                }
                if (runForPositions.isEmpty()) return@forEach
            }
        }
    }

    fun cancel() = job.cancel()
}
