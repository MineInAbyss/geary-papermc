package com.mineinabyss.geary.papermc.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.actions.main
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.InChunkLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnLocationChooser
import com.mineinabyss.geary.papermc.spawning.choosing.SpreadChunkChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.SpawningWorldGuardFlags
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnSectionsConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawningSchema
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import com.mineinabyss.geary.papermc.spawning.spawn_types.geary.GearySpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.mythic.MythicSpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.spawning.targeted.ListSpawnListener
import com.mineinabyss.geary.papermc.spawning.listeners.SpreadEntityDeathListener
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.geary.papermc.spawning.tasks.SpreadSpawnTask
import com.mineinabyss.geary.papermc.sqlite.sqliteDatabase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.config
import com.sk89q.worldguard.WorldGuard
import kotlin.io.path.Path
import me.dvyy.sqlite.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getWorld
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class SpawningFeature(context: FeatureContext) : Feature(context) {
    val config by config("spawning", plugin.dataPath, SpawnConfig())
    val spreadConfig by config("spread_config", plugin.dataPath,
        SpreadSpawnSectionsConfig())
    var spawnTask: SpawnTask? = null
    var spawnEntriesByName: Map<String, SpawnEntry>? = null
    var spreadSpawnTask: SpreadSpawnTask? = null
    var database: Database? = null


    init {
        pluginDeps("WorldGuard", "MythicMobs")
        println("SpawningFeature initialized")
    }

    override fun canEnable() = gearyPaper.config.spawning

    override fun load() {
        println("SpawningFeature load")
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

        // -- Spread Spawn logic --
        val mainWorld = getWorld(spreadConfig.WorldName) ?: error("World ${spreadConfig.WorldName} not found, cannot initialize spread spawning")
        val posChooser = InChunkLocationChooser(task.mobSpawner, mainWorld)
        val chunkChooser = SpreadChunkChooser(mainWorld)

        val spreadSpawner = SpreadSpawner(
            db = db,
            world = Bukkit.getWorld("world")!!,
            configs = spreadConfig,
            chunkChooser = chunkChooser,
            posChooser = posChooser,
            dao = SpawnLocationsDAO()
        )

        listeners(
            GearySpawnTypeListener(),
            MythicSpawnTypeListener(),
            ListSpawnListener(spreadSpawner, db, plugin),
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

    fun dumpDB(loc: Location, player : Player?) {
        val db = database ?: return println("no database to dump")
        val dao = spreadSpawnTask?.spreadSpawner?.dao ?: return println("no spread spawner to dump db from")
        if (player == null)
            return println("no player to dump db to")
        println("spread config is $spreadConfig")
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

    fun clearDB(world: World) {
        val db = database ?: return println("no database to clear")
        val dao = spreadSpawnTask?.spreadSpawner?.dao ?: return println("no spread spawner to clear db from")
        plugin.launch {
            db.write {
                dao.dropAll(world)
            }
        }
        println("Cleared spawn locations from the database.")
    }


}
