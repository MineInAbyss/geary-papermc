package com.mineinabyss.geary.papermc.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.*
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.SpawningWorldGuardFlags
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawningSchema
import com.mineinabyss.geary.papermc.spawning.listeners.ListSpawnListener
import com.mineinabyss.geary.papermc.spawning.listeners.SpreadEntityDeathListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.geary.GearySpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.mythic.MythicSpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.geary.papermc.spawning.tasks.SpreadSpawnTask
import com.mineinabyss.geary.papermc.sqlite.sqliteDatabase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.sk89q.worldguard.WorldGuard
import me.dvyy.sqlite.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import kotlin.io.path.Path

class SpawningFeature(context: FeatureContext) : Feature(context) {
    val config by config("spawning", plugin.dataPath, SpawnConfig())
    var spawnTask: SpawnTask? = null
    var spawnEntriesByName: Map<String, SpawnEntry>? = null
    var spreadSpawnTask: SpreadSpawnTask? = null
    var database: Database? = null

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
        // -- Database logic --
        val db = plugin.sqliteDatabase(Path("spawns.db")) {
            val world = Bukkit.getWorlds().firstOrNull() ?: error("No worlds found, cannot initialize spawning database")
            SpawningSchema(listOf(world)).init()
        }
        database = db

        listeners(
            GearySpawnTypeListener(),
            MythicSpawnTypeListener(),
        )

        // -- Regular spawning logic --
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
        val mobSpawner = MobSpawner(spawnChooser, LocationSpread(triesForNearbyLoc = 10))
        val task = SpawnTask(
            runTimes = config.runTimes,
            locationChooser = SpawnLocationChooser(config.range),
            spawnAttempts = config.maxSpawnAttemptsPerPlayer,
            mobSpawner = mobSpawner,
        )

        // -- Spread Spawn logic --
        val spreadConfig by config(
            "spread_config", plugin.dataPath,
            SpreadEntityTypesConfig(),
            mergeUpdates = false,
            formats = ConfigFormats(
                listOf(
                    Format(
                        "yml", Yaml(
                            serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).serializers.module,
                            configuration = YamlConfiguration(strictMode = false)
                        )
                    )
                )
            )
        )
        val mainWorld = Bukkit.getWorld(spreadConfig.worldName) ?: error("World ${spreadConfig.worldName} not found, cannot initialize spread spawning")
        val posChooser = InChunkLocationChooser(mobSpawner, mainWorld)
        val dao = SpawnLocationsDAO()
        val chunkChooser = SpreadChunkChooser(mainWorld, db, dao)

        val spreadSpawner = SpreadSpawner(
            db = db,
            world = Bukkit.getWorld("world")!!,
            configs = spreadConfig,
            chunkChooser = chunkChooser,
            posChooser = posChooser,
            dao = dao,
            logger = logger
        )

        listeners(
            ListSpawnListener(spreadSpawner, db, dao ,plugin),
            SpreadEntityDeathListener(
                spreadSpawner, db, plugin, mainWorld
            )
        )

        val spreadTask = SpreadSpawnTask(
            world = Bukkit.getWorlds().firstOrNull() ?: error("No worlds found, cannot initialize spread spawning"),
            configs = spreadConfig,
            spreadSpawner = spreadSpawner
        )

        // -- Tasks registration --
        spawnTask = task
        spreadSpawnTask = spreadTask
        spawnEntriesByName = spawns.mapValues { it.value.entry }
        task(task.job)
        task(spreadTask.job)
    }

    fun sendTpButton(player: Player, loc: Location) {
        val command = "/tp ${loc.x} ${loc.y} ${loc.z}"
        val distance = if (loc.world != null) loc.distance(player.location).toInt() else -1
        val message = Component.text("TP to (${loc.x}, ${loc.y}, ${loc.z}) ($distance blocks away)")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
        player.sendMessage(message)
    }

    fun dumpDB(loc: Location, player : Player) {
        val db = database ?: error("No database to dump")
        val dao = SpawnLocationsDAO()
        plugin.launch {
            db.read {
                val locations = dao.getSpawnsNear(loc, 10000.0)
                player.sendMessage("Total spawn locations: ${locations.size}")
                locations.forEach { location ->
                    sendTpButton(player, location.location)
                }
            }
        }
    }

    // the db is locked when we try to run this function.
    fun clearDB(world: World) {
        val db = database ?: return println("no database to clear")
        val dao = SpawnLocationsDAO()
        plugin.launch {
            db.write {
                dao.dropAll(world)
            }
        }
    }
}
