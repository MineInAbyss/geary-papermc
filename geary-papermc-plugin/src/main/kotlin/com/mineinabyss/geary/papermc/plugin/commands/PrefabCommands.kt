package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabLoader.PrefabLoadResult
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

internal fun IdoCommand.prefabs() = "prefab" {
    permission = "geary.admin.prefab"
    "count" {
        executes.args("prefab" to GearyArgs.prefab()) { prefab ->
            val geary = gearyPaper.worldManager.global
            with(geary) {
                val count = geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefab)
                    not { has<PrefabKey>() }
                }).count()
                sender.success("There are $count direct instances of ${prefab.get<PrefabKey>()}")
            }
        }
    }
    "reload" {
        executes.args("prefab" to GearyArgs.prefab()) { prefab ->
            with(gearyPaper.worldManager.global) {
                runCatching { getAddon(Prefabs).loader.reload(prefab) }
                    .onSuccess { sender.success("Reread prefab $prefab") }
                    .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }


                // Reload entities
                findEntities {
                    hasRelation<InstanceOf?>(prefab)
                    has<BukkitEntity>()
                }.forEach {
                    UpdateMob.recreateGearyEntity(it.get<BukkitEntity>() ?: return@forEach)
                }

                // Reload items
                findEntities {
                    hasRelation<InstanceOf?>(prefab)
                    has<ItemStack>()
                }.toSet()
                    .mapNotNull { it.parent }
                    .forEach { it.get<Player>()?.inventory?.toGeary()?.forceRefresh(ignoreCached = true) }
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
            val prefabs = gearyPaper.worldManager.global.getAddon(Prefabs)
            // Ensure not already registered
            if (prefabs.manager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                fail("Prefab $namespace:$path already exists")
            }

            // Try to load from file
            val load = prefabs.loader.loadFromPath(
                namespace,
                kotlinx.io.files.Path(gearyPaper.plugin.dataFolder.resolve(namespace).resolve(path).path)
            )
            when (load) {
                is PrefabLoadResult.Failure -> {
                    sender.error("Failed to read prefab $namespace:$path:\n${load.error.message}")
                }

                is PrefabLoadResult.Success -> {
                    load.entity.inheritPrefabsIfNeeded()
                    sender.success("Read prefab $namespace:$path")
                }

                is PrefabLoadResult.Warn -> {
                    load.entity.inheritPrefabsIfNeeded()
                    sender.warn("Read prefab $namespace:$path with warnings")
                }

                is PrefabLoadResult.Defer -> {
                    sender.error("Failed to read prefab $namespace:$path:\n Dependent on unloaded prefab")
                }
            }
        }
    }
}
