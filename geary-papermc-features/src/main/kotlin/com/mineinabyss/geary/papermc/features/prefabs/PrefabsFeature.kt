package com.mineinabyss.geary.papermc.features.prefabs

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.WorldManager
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabLoader.PrefabLoadResult
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.PrefabsModuleExtensions.fromDirectory
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import kotlin.io.path.*

//TODO move geary.configure definition into here as well
val PrefabsFeature = module("prefabs") {
    require(get<GearyPaperConfig>().loading.prefabs) { "Prefabs must be enabled in config" }
    val plugin = get<Plugin>()

    gearyWorld {
        val prefabs = getAddon(Prefabs)
        // Flagged out here rather than inside the coroutine, a plugin asking whether prefabs are still
        // loading may well have asked before that first delay is even up
        PrefabLoading.started()
        // Load prefabs in Geary/prefabs folder, each subfolder is considered its own namespace
        plugin.launch {
            try {
                delay(1.ticks) // Let other plugins register components

                plugin.dataPath
                    .resolve("prefabs")
                    .createDirectories()
                    .listDirectoryEntries()
                    .filter(Path::isDirectory)
                    .forEach { folder ->
                        prefabs.fromDirectory(folder.name, folder)
                    }

                // Force item refresh if any players are online
                plugin.server.onlinePlayers.forEach { it.inventory.toGeary()?.forceRefresh(ignoreCached = true) }
            } finally {
                // A namespace throwing still ends the load, whoever waits on it must not hang
                PrefabLoading.finished()
            }
        }
    }
}.mainCommand {
    "prefab" {
        permission = "geary.admin.prefab"
        "count" {
            executes.args("prefab" to GearyArgs.prefab()) { prefab ->
                val geary = get<WorldManager>().global
                with(geary) {
                    val count = geary.queryManager.getEntitiesMatching(family {
                        hasRelation<InstanceOf?>(prefab)
                        not { has<PrefabKey>() }
                    }).count()
                    sender.success("There are <aqua>$count</aqua> direct instances of <gold>${prefab.get<PrefabKey>()}</gold>")
                }
            }
        }
        "reload" {
            executes.args("prefab" to GearyArgs.prefab()) { prefab ->
                gearyPaper.forEachWorld {
                    val key = prefab.get<PrefabKey>() ?: prefab
                    runCatching { getAddon(Prefabs).loader.reload(prefab) }
                        .onFailure { fail("Failed to reread prefab <gold>$key</gold>:\n${it.message}") }

                    // Reload entities
                    var entityCount = 0
                    findEntities {
                        hasRelation<InstanceOf?>(prefab)
                        has<BukkitEntity>()
                    }.forEach {
                        UpdateMob.recreateGearyEntity(it.get<BukkitEntity>() ?: return@forEach)
                        entityCount++
                    }

                    // Reload items
                    val players = findEntities {
                        hasRelation<InstanceOf?>(prefab)
                        has<ItemStack>()
                    }.toSet()
                        .mapNotNull { it.parent }
                        .mapNotNull { it.get<Player>() }
                        .toSet()
                    players.forEach { it.inventory.toGeary()?.forceRefresh(ignoreCached = true) }

                    sender.success("Reread prefab <gold>$key</gold>, updated <yellow>$entityCount</yellow> entities and items for <aqua>${players.size}</aqua> players")
                }
            }
        }
        "load" {
            executes.args(
                "namespace" to GearyArgs.namespace(),
                "path" to Args.word().suggests {
                    //TODO get previous argument in suggestion
                    val namespace = input.split(" ").dropLast(1).lastOrNull() ?: return@suggests
//                plugin.dataFolder.resolve("prefabs").resolve(namespace).walk()
//                    .filter {
//                        it.name.startsWith(args[3].lowercase()) && it.extension == "yml" && prefabManager[PrefabKey.of(
//                            args[2],
//                            it.nameWithoutExtension
//                        )] == null
//                    }.map {
//                        it.relativeTo(plugin.dataFolder.resolve(args[2])).toString()
//                    }
//                    .toList()
                }
            ) { namespace, path ->
                gearyPaper.forEachWorld {
                    val prefabs = getAddon(Prefabs)
                    // Ensure not already registered
                    if (prefabs[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                        fail("Prefab <gold>$namespace:$path</gold> already exists")
                    }

                    // Try to load from file
                    val load = prefabs.loader.loadFromPath(
                        namespace,
                        kotlinx.io.files.Path(gearyPaper.dataFolder.resolve(namespace).resolve(path).path)
                    )
                    when (load) {
                        is PrefabLoadResult.Failure -> {
                            sender.error("Failed to read prefab <gold>$namespace:$path</gold>:\n${load.error.message}")
                        }

                        is PrefabLoadResult.Success -> {
                            load.entity.inheritPrefabsIfNeeded()
                            sender.success("Read prefab <gold>$namespace:$path</gold>")
                        }

                        is PrefabLoadResult.Warn -> {
                            load.entity.inheritPrefabsIfNeeded()
                            sender.warn("Read prefab <gold>$namespace:$path</gold> with warnings")
                        }

                        is PrefabLoadResult.Defer -> {
                            sender.error("Failed to read prefab $namespace:$path:\n Dependent on unloaded prefab")
                        }
                    }
                }
            }
        }
    }
}