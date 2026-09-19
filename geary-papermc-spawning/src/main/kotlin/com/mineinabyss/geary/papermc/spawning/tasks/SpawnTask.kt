package com.mineinabyss.geary.papermc.spawning.tasks

import com.mineinabyss.geary.papermc.spawning.MobSpawner
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.time.inWholeTicks
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.GameMode.SPECTATOR
import org.bukkit.plugin.Plugin
import java.util.EnumMap

class SpawnTask(
    config: SpawnConfig,
    plugin: Plugin,
    logger: ComponentLogger,
    private val locationChooser: SpawnLocationChooser,
    private val mobSpawner: MobSpawner,
    private val mobCaps: MobCaps,
) {
    private val runTimeTicks: Map<SpawnPosition, Long> = config.runTimes.mapValues { it.value.inWholeTicks.coerceAtLeast(1) }
    private val lastRunTick = EnumMap<SpawnPosition, Int>(SpawnPosition::class.java)
    private val spawnAttempts: Int = config.maxSpawnAttemptsPerPlayer
    private val spawnDelayTicks = config.spawnDelay.inWholeTicks.coerceAtLeast(1)

    val job: Job = Job().also { job ->
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            runCatching { run() }.onFailure {
                logger.e { "Spawn task failed: ${it.stackTraceToString()}" }
            }
        }, 20, spawnDelayTicks)
        job.invokeOnCompletion { task.cancel() }
    }

    fun run() {
        val currTick = Bukkit.getCurrentTick()
        val allowedSpawnPositions = SpawnPosition.entries.filter { position ->
            val last = lastRunTick[position]
            last == null || currTick - last >= runTimeTicks.getOrDefault(position, 1L)
        }.ifEmpty { return }
        allowedSpawnPositions.forEach { lastRunTick[it] = currTick }
        val onlinePlayers = Bukkit.getOnlinePlayers().filter { !it.isDead && it.gameMode != SPECTATOR }

        onlinePlayers.forEach { player ->
            val attemptedPositions = allowedSpawnPositions.toMutableSet()
            // Candidates are within maxDistance of the player, assumed inside playerCapRadius, so they share one count
            val categoryCounts = lazy { mobCaps.countsNear(player.location) }
            repeat(spawnAttempts) {
                if (attemptedPositions.isEmpty()) return@forEach
                val spawnLoc = locationChooser.chooseSpawnLocationNear(onlinePlayers, player.location) ?: return@repeat
                val type = SpawnPositionReader.spawnPositionFor(spawnLoc)

                if (attemptedPositions.remove(type)) {
                    mobSpawner.attemptSpawnAt(spawnLoc, type, categoryCounts)
                }
            }
        }
    }
}
