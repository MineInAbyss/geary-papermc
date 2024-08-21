package com.mineinabyss.geary.papermc.spawning

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.idofront.config.config

class SpawningFeature {
    val config = config("spawning.yml", gearyPaper.plugin.dataPath, SpawnConfig())

    fun install() {
        val config = config.getOrLoad()
        val spawns = listOf<SpawnEntry>() // TODO read spawns
        val wg = WorldGuardSpawning(spawns)
        val caps = MobCaps(config.playerCaps, config.range.playerCapRadius)
        val spawnChooser = SpawnChooser(wg, caps)
        val spawnTask = SpawnTask(
            runTimes = config.runTimes,
            locationChooser = SpawnLocationChooser(config.range),
            spawnPositionReader = SpawnPositionReader(),
            spawnAttempts = config.maxSpawnAttemptsPerPlayer,
            mobSpawner = MobSpawner(spawnChooser),
        )
    }
}
