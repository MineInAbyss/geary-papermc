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
import com.mineinabyss.geary.papermc.spawning.config.*
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
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
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

    val locationConfigReader = config<SpawnLocationsConfig> {
        format = get<Yaml>()
    }.multiEntry((plugin.dataPath / "locations").createParentDirectories())


    val locationConfig: SpawnLocationsUnified by single {
        val entries = locationConfigReader.read()
        SpawnLocationsUnified(entries)
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

    listeners(
        spawnTypeListener,
        mythicSpawnListener,
        listSpawnListener,
        spreadDeathListener,
    )

    // -- Tasks registration --
    task(get<SpawnTask>().job)
    task(get<SpreadSpawnTask>().job)

    val logger = get<Logger>()


    plugin.launch {
        delay(1.ticks) // Let other plugins register components
        context // Load context (reads all spawns)
        logger.i { "Loaded ${context.spawns.size} normal spawn types and ${spreadConfig.types.size} spread spawn types" }
    }
}.mainCommand {
    "spawns" {
        "getNearbyDBEntries" {
            executes.asPlayer {
                println("[Geary] - Querying DB entries")
                get<SpawningContext>().dumpDB(player.location, player)
            }
        }
        "clearDB" {
            executes.asPlayer {
                get<Plugin>().launch { get<SpreadSpawnRepository>().dropAll(player.world) }
                sender.success("Cleared spawn locations from the database.")
            }
        }

        "listRegions" {
            executes.asPlayer {
                val unified = get<SpawnLocationsUnified>().unified
                if (unified.isEmpty()) {
                    sender.info("No spawn location regions configured.")
                } else {
                    sender.info("<gray>${unified.size} region(s):</gray>".miniMsg())
                    unified.forEach { (id, region) ->
                        val cx: Int
                        val cy: Int
                        val cz: Int
                        if (region.center != null) {
                            cx = region.center.blockX
                            cy = region.center.blockY
                            cz = region.center.blockZ
                        } else {
                            cx = ((region.locMin.x + region.locMax.x) / 2).toInt()
                            cy = ((region.locMin.y + region.locMax.y) / 2).toInt()
                            cz = ((region.locMin.z + region.locMax.z) / 2).toInt()
                        }
                        val tag = if (region.group != null) " <gray>[${region.group}]</gray>" else ""
                        val override = if (region.gearySpawnOverride) " <yellow>(override)</yellow>" else ""
                        val hoverLines = buildList {
                            add("center: <gray>[$cx, $cy, $cz]</gray>")
                            if (region.center == null)
                                add("bounds: <gray>[${region.locMin.blockX}, ${region.locMin.blockY}, ${region.locMin.blockZ}]</gray> - <gray>[${region.locMax.blockX}, ${region.locMax.blockY}, ${region.locMax.blockZ}]</gray>")
                            else if (region.radius != null)
                                add("radius: <gray>${region.radius}${if (region.radiusY != null) " / ${region.radiusY}Y" else ""}</gray>")
                        }.joinToString("\n")
                        val msg = " <white>$id</white>$tag$override <dark_gray>[click to TP]</dark_gray>".miniMsg()
                            .hoverEvent(HoverEvent.showText(hoverLines.miniMsg()))
                            .clickEvent(ClickEvent.runCommand("/tp $cx $cy $cz"))
                        player.sendMessage(msg)
                    }
                }
            }
        }

        "test" {
            "location" {
                executes.asPlayer {
                    val unified = get<SpawnLocationsUnified>().unified
                    val matching = unified.entries.filter { (_, region) -> region.isInside(player.location) }
                    if (matching.isEmpty()) {
                        sender.info("Not inside any configured spawn location region.")
                    } else {
                        sender.success("Inside ${matching.size} region(s):")
                        matching.forEach { (id, region) ->
                            val tag = if (region.group != null) " [${region.group}]" else ""
                            val override = if (region.gearySpawnOverride) " (override)" else ""
                            sender.info("  - $id$tag$override")
                        }
                    }
                }
            }
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
