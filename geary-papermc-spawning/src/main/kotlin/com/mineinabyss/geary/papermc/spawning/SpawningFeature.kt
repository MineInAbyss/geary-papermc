package com.mineinabyss.geary.papermc.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.SpawningWorldGuardFlags
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawningSchema
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spawn_types.geary.GearySpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.mythic.MythicSpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.geary.papermc.sqlite.sqliteDatabase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.config
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Bukkit
import kotlin.io.path.Path

class SpawningFeature(context: FeatureContext) : Feature(context) {
    val config by config("spawning", plugin.dataPath, SpawnConfig())
    var spawnTask: SpawnTask? = null
    var spawnEntriesByName: Map<String, SpawnEntry>? = null
    val db = plugin.sqliteDatabase(Path("spawns.db")) {
        SpawningSchema(listOf(Bukkit.getWorld("world")!!)).init()
    }

    init {
        pluginDeps("WorldGuard", "MythicMobs")
    }

    override fun canEnable() = gearyPaper.config.spawning

    override fun load() {
        runCatching {
            val registry = WorldGuard.getInstance().flagRegistry
            registry.register(SpawningWorldGuardFlags.OVERRIDE_LOWER_PRIORITY_SPAWNS)
        }.onFailure {
            logger.w { "Failed to register WorldGuard flags for Geary spawning" }
            it.printStackTrace()
        }
    }

    override fun enable() {
        listeners(
            GearySpawnTypeListener(),
            MythicSpawnTypeListener(),
        )

        val reader = SpawnEntryReader(
            gearyPaper.plugin, Yaml(
                serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).serializers.module,
                configuration = YamlConfiguration(
                    strictMode = false
                )
            )
        )
        val spawns = reader.readSpawnEntries()
        val wg = WorldGuardSpawning(spawns.values.map { it.entry })
        val caps = MobCaps(config.playerCaps, config.defaultCap, config.range.playerCapRadius)
        val spawnChooser = SpawnChooser(wg, caps)
        val spawnPositionReader = SpawnPositionReader()
        val task = SpawnTask(
            runTimes = config.runTimes,
            locationChooser = SpawnLocationChooser(config.range),
            spawnPositionReader = spawnPositionReader,
            spawnAttempts = config.maxSpawnAttemptsPerPlayer,
            mobSpawner = MobSpawner(
                spawnChooser,
                LocationSpread(spawnPositionReader, triesForNearbyLoc = 10)
            ),
        )
        spawnTask = task
        spawnEntriesByName = spawns.mapValues { it.value.entry }
        task(task.job)
    }
}
