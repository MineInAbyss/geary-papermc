package com.mineinabyss.geary.papermc.spawning

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spawn_types.geary.GearySpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.mythic.MythicSpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.plugin.Plugins

class SpawningFeature(context: FeatureContext) : Feature(context) {
    val config by config("spawning", plugin.dataPath, SpawnConfig())

    override fun canEnable() = when {
        !gearyPaper.config.spawning -> false
        !(Plugins.isEnabled("WorldGuard") && Plugins.isEnabled("MythicMobs")) -> {
            logger.w { "WorldGuard and MythicMobs are required for the spawning system to work" }
            false
        }

        else -> true
    }

    override fun enable() {
        listeners(
            GearySpawnTypeListener(),
            MythicSpawnTypeListener(),
        )

        val reader = SpawnEntryReader(gearyPaper.plugin)
        val spawns = reader.readSpawnEntries()
        val wg = WorldGuardSpawning(spawns)
        val caps = MobCaps(config.playerCaps, config.range.playerCapRadius)
        val spawnChooser = SpawnChooser(wg, caps)

        task(
            SpawnTask(
                runTimes = config.runTimes,
                locationChooser = SpawnLocationChooser(config.range),
                spawnPositionReader = SpawnPositionReader(),
                spawnAttempts = config.maxSpawnAttemptsPerPlayer,
                mobSpawner = MobSpawner(spawnChooser),
            ).job
        )
    }
}
