package com.mineinabyss.geary.papermc.spawning

import co.touchlab.kermit.Logger
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.data.SpawnsDatabase
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.*
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.geary.papermc.spawning.listeners.ListSpawnListener
import com.mineinabyss.geary.papermc.spawning.listeners.SpreadEntityDeathListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.geary.GearySpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spawn_types.mythic.MythicSpawnTypeListener
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.spawning.tasks.SpawnTask
import com.mineinabyss.geary.papermc.spawning.tasks.SpreadSpawnTask
import com.mineinabyss.geary.papermc.sqlite.sqliteDatabase
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div

val SpawningFeature = module("spawning") {
    requirePlugins("WorldGuard", "MythicMobs")
    require(get<GearyPaperConfig>().spawning) { "Spawning must be enabled in config" }

    // -- Configs --
    val spawning by singleConfig<SpawnConfig>("spawning.yml") { default = SpawnConfig() }
    val spreadConfig by singleConfig<SpreadEntityTypesConfig>("spread_config.yml") {
        withSerializersModule(gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module)
        default = SpreadEntityTypesConfig()
    }

    val locationConfigReader = config<SpawnLocationsConfig> {
        format = get<Yaml>()
    }.multiEntry((plugin.dataPath / "locations").createParentDirectories())

    val locationConfig: SpawnLocationsUnified by single {
        val entries = locationConfigReader.read()
        SpawnLocationsUnified(entries)
    }

    single {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
    single {
        Yaml(
            serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
            configuration = YamlConfiguration(
                strictMode = false
            )
        )
    }

    single { Bukkit.getWorld(spreadConfig.worldName) ?: error("Spawn config main world not found!") }

    // -- Configure database --
    val spawnDAO by single { new(::SpawnsDatabase) }
    val spawnDB by single<Database> {
        get<Plugin>().sqliteDatabase(Path("spawns.db")) {
            spawnDAO.create()
        }
    }
    single { spawnDAO.spawnQueries }
    single { new(::SpreadSpawnRepository) }

    // -- Regular spawning logic --
    single { new(::SpawnEntryReader) }
    single { new(::WorldGuardSpawning) }
    single { new(::MobCaps) }
    single { new(::SpawnChooser) }
    single { LocationSpread(triesForNearbyLoc = 10) }
    single { new(::MobSpawner) }
    single { new(::SpawnLocationChooser) }

    // -- Spread Spawn logic --
    val context by single { new(::SpawningContext) }
    single { new(::InChunkLocationChooser) }
    single { new(::SpreadChunkChooser) }
    single { new(::SpreadSpawner) }

    single { new(::SpawnTask) }
    single { new(::SpreadSpawnTask) }


    // -- Listeners --
    val spawnTypeListener by single { new(::GearySpawnTypeListener) }
    val mythicSpawnListener by single { new(::MythicSpawnTypeListener) }
    val listSpawnListener by single { new(::ListSpawnListener) }
    val spreadDeathListener by single { new(::SpreadEntityDeathListener) }

    val logger = get<Logger>()
    logger.i { "Loaded ${context.spawns.size} normal spawn types and ${spreadConfig.types.size} spread spawn types" }
    listeners(
        spawnTypeListener,
        mythicSpawnListener,
        listSpawnListener,
        spreadDeathListener,
    )

    // -- Tasks registration --
    task(get<SpawnTask>().job)
    task(get<SpreadSpawnTask>().job)

}.mainCommand {
    "spawns" {
        "getNearbyDBEntries" {
            executes.asPlayer {
                get<SpawningContext>().dumpDB(player.location, player)
            }
        }
        "clearDB" {
            executes.asPlayer {
                get<Plugin>().launch { get<SpreadSpawnRepository>().dropAll(player.world) }
                sender.success("Cleared spawn locations from the database.")
            }
        }

        "test" {
            executes.asPlayer().args(
                "Spawn name" to Args.string().suggests { suggestFiltering(get<SpawningContext>().spawnEntriesByName.map { it.key }) }
            ) { spawnName ->
                val mobSpawner = get<MobSpawner>()
                val spawn = get<SpawningContext>().spawnEntriesByName.get(spawnName) ?: fail("Could not find spawn named $spawnName")
                runCatching { mobSpawner.checkSpawnConditions(spawn, player.location) }
                    .onSuccess {
                        if (it) sender.success("Conditions for $spawnName passed") else sender.error("Conditions for $spawnName failed")
                    }
                    .onFailure { sender.error("Conditions for $spawnName failed:\n${it.message}") }
            }
        }
    }
}
