package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabLoader.PrefabLoadResult
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import okio.Path.Companion.toOkioPath
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

internal fun IdoCommand.prefabs() = "prefab" {
    requiresPermission("geary.admin.prefab")
    "count" {
        val prefabArg by GearyArgs.prefab()

        executes {
            val geary = gearyPaper.worldManager.global
            with(geary) {
                val prefab = entityOfOrNull(PrefabKey.of(prefabArg().asString()))
                    ?: commandException("No such prefab $prefabArg")
                val count = geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefab)
                    not { has<PrefabKey>() }
                }).count()
                sender.success("There are $count direct instances of ${prefabArg().asString()}")
            }
        }
    }
    "reload" {
        val prefabArg by GearyArgs.prefab()

        executes {
            with(gearyPaper.worldManager.global) {
                val prefab = PrefabKey.of(prefabArg().asString())
                val prefabEntity = entityOfOrNull(prefab) ?: commandException("No such prefab $prefabArg")
                runCatching { getAddon(Prefabs).loader.reload(prefabEntity) }
                    .onSuccess { sender.success("Reread prefab $prefab") }
                    .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }


                // Reload entities
                findEntities {
                    hasRelation<InstanceOf?>(prefabEntity)
                    has<BukkitEntity>()
                }.forEach {
                    UpdateMob.recreateGearyEntity(it.get<BukkitEntity>() ?: return@forEach)
                }

                // Reload items
                findEntities {
                hasRelation<InstanceOf?>(prefabEntity)
                    has<ItemStack>()
                }.toSet()
                    .mapNotNull { it.parent }
                    .forEach { it.get<Player>()?.inventory?.toGeary()?.forceRefresh(ignoreCached = true) }
            }
        }
    }
    "load" {
        val namespaceArg by Args.word().suggests {
            suggest(plugin.dataFolder.resolve("prefabs").listFiles()?.filter {
                it.isDirectory && it.name.startsWith(suggestions.remaining.lowercase())
            }?.map { it.name } ?: emptyList())
        }
        val pathArg by Args.word().suggests {
            //TODO get previous argument in suggestion
//                plugin.dataFolder.resolve("prefabs").resolve(namespaceArg()).walk()
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
        executes {
            val namespace = namespaceArg()
            val path = pathArg()

            val prefabs = gearyPaper.worldManager.global.getAddon(Prefabs)
            // Ensure not already registered
            if (prefabs.manager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                commandException("Prefab $namespace:$path already exists")
            }

            // Try to load from file
            val load = prefabs.loader.loadFromPath(
                namespace,
                gearyPaper.plugin.dataFolder.resolve(namespace).resolve(path).toOkioPath()
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
            }
        }
    }
}
