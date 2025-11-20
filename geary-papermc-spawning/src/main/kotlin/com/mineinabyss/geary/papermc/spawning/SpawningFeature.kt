package com.mineinabyss.geary.papermc.spawning

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.choosing.*
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.SpawningWorldGuardFlags
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntryReader
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
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
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.sk89q.worldguard.WorldGuard
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import org.bukkit.Bukkit
import org.bukkit.World
import org.koin.core.module.dsl.scopedOf
import kotlin.io.path.Path

val SpawningFeature = feature("spawning") {
    dependsOn {
        plugins("WorldGuard", "MythicMobs")
        condition { get<GearyPaperConfig>().spawning }
    }

    scopedModule {
        scopedConfig<SpawnConfig>("spawning.yml") { default = SpawnConfig() }
        scopedConfig<SpreadEntityTypesConfig>("spread_config.yml") {
            withSerializersModule(get<Geary>().getAddon(SerializableComponents).serializers.module)
            default = SpreadEntityTypesConfig()
        }
        scoped<Database> {
            // -- Database logic --
            plugin.sqliteDatabase(Path("spawns.db")) {
                val world = Bukkit.getWorlds().firstOrNull() ?: error("No worlds found, cannot initialize spawning database")
                SpawningSchema(listOf(world)).init()
            }
        }

        scoped<Json> {
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        }
        scoped<Yaml> {
            Yaml(
                serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).serializers.module,
                configuration = YamlConfiguration(
                    strictMode = false
                )
            )
        }
        scoped<World> {
            Bukkit.getWorld(get<SpreadEntityTypesConfig>().worldName) ?: error("Spawn config main world not found!")
        }


        // -- Regular spawning logic --
        scopedOf(::SpawnEntryReader)
        scopedOf(::SpawnEntryReader)
        scopedOf(::WorldGuardSpawning)
        scopedOf(::MobCaps)
        scopedOf(::SpawnChooser)
        scoped { LocationSpread(triesForNearbyLoc = 10) }
        scopedOf(::MobSpawner)
        scopedOf(::SpawnLocationChooser)
        scopedOf(::SpawnTask)

        // -- Spread Spawn logic --
        scopedOf(::SpawningContext)
        scopedOf(::InChunkLocationChooser)
        scopedOf(::SpawnLocationsDAO)
        scopedOf(::SpreadChunkChooser)
        scopedOf(::SpreadSpawner)

        scopedOf(::SpawnTask)
        scopedOf(::SpreadSpawnTask)

        // -- Listeners --
        scopedOf(::GearySpawnTypeListener)
        scopedOf(::MythicSpawnTypeListener)
        scopedOf(::ListSpawnListener)
        scopedOf(::SpreadEntityDeathListener)
        scopedOf(::ListSpawnListener)
        scopedOf(::SpreadEntityDeathListener)
    }


    onLoad {
        runCatching {
            val registry = WorldGuard.getInstance().flagRegistry
            registry.register(SpawningWorldGuardFlags.OVERRIDE_LOWER_PRIORITY_SPAWNS)
        }.onFailure {
            get<ComponentLogger>().w { "Failed to register WorldGuard flags for Geary spawning" }
            it.printStackTrace()
        }
    }

    onEnable {
        listeners(
            get<GearySpawnTypeListener>(),
            get<MythicSpawnTypeListener>(),
            get<ListSpawnListener>(),
            get<SpreadEntityDeathListener>(),
        )

        // -- Tasks registration --
        task(get<SpawnTask>().job)
        task(get<SpreadSpawnTask>().job)
    }

    mainCommand {
        "spawns" {
            "getNearbyDBEntries" {
                executes.asPlayer {
                    get<SpawningContext>().dumpDB(player.location, player)
                }
            }
            "clearDB" {
                executes.asPlayer {
                    get<SpawningContext>().clearDB(player.world)
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
}
