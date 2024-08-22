package com.mineinabyss.geary.papermc.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
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
import com.mineinabyss.geary.serialization.serializableComponents
import com.mineinabyss.idofront.config.config

class SpawningFeature(context: FeatureContext) : Feature(context) {
    val config by config("spawning", plugin.dataPath, SpawnConfig())

    init {
        pluginDeps("WorldGuard", "MythicMobs")
    }

    override fun canEnable() = gearyPaper.config.spawning

    override fun enable() {
        listeners(
            GearySpawnTypeListener(),
            MythicSpawnTypeListener(),
        )

        val reader = SpawnEntryReader(
            gearyPaper.plugin, Yaml(
                serializersModule = serializableComponents.serializers.module,
                configuration = YamlConfiguration(
                    strictMode = false
                )
            )
        )
        val spawns = reader.readSpawnEntries()
        val wg = WorldGuardSpawning(spawns)
        val caps = MobCaps(config.playerCaps, config.defaultCap, config.range.playerCapRadius)
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
