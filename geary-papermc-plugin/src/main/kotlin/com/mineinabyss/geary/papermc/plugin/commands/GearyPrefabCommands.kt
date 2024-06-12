package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.commands.GearyCommands.filterPrefabs
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabLoader.PrefabLoadResult
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabsIfNeeded
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mojang.brigadier.arguments.StringArgumentType
import okio.Path.Companion.toOkioPath
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

private val prefabLoader get() = prefabs.loader
private val prefabManager get() = prefabs.manager

fun IdoRootCommand.prefabs() {
    "prefab" {
        "reload" {
            val prefab by PrefabKeyArgument().suggests {
                suggest(prefabManager.keys.filterPrefabs(context.input.substringAfterLast(" ")).toList())
            }
            executes {
                val prefab = prefab()!!
                val prefabEntity = prefab.toEntityOrNull() ?:
                    return@executes sender.error("Requested non null prefab entity for $prefab, but it does not exist.")
                runCatching { prefabLoader.reload(prefabEntity) }
                    .onSuccess { sender.success("Reread prefab $prefab") }
                    .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }


                // Reload entities
                geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefabEntity)
                    has<BukkitEntity>()
                }).forEach {
                    UpdateMob.recreateGearyEntity(it.get<BukkitEntity>() ?: return@forEach)
                }

                // Reload items
                geary.queryManager.getEntitiesMatching(family {
                    hasRelation<InstanceOf?>(prefabEntity)
                    has<ItemStack>()
                }).toSet()
                    .mapNotNull { it.parent }
                    .forEach { it.get<Player>()?.inventory?.toGeary()?.forceRefresh(ignoreCached = true) }
            }
        }
        "load" {
            val namespace by StringArgumentType.word().suggests {
                suggest(plugin.dataFolder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: listOf())
            }
            val path by StringArgumentType.greedyString().suggests {
                val namespace = context.lastChild.input
                val current = context.input
                suggest(plugin.dataFolder.resolve(namespace).walk().filter {
                        it.extension == "yml"
                                && it.nameWithoutExtension.startsWith(current.lowercase())
                                && prefabManager[PrefabKey.of(namespace, it.nameWithoutExtension)] == null
                    }.map { it.relativeTo(plugin.dataFolder.resolve(namespace)).toString() }.toList()
                )
            }
            executes {
                val (namespace, path) = namespace()!! to path()!!
                // Ensure not already registered
                if (prefabManager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                    sender.error("Prefab $namespace:$path already exists")
                    return@executes
                }

                // Try to load from file
                val load = prefabLoader.loadFromPath(namespace, gearyPaper.plugin.dataFolder.resolve(namespace).resolve(path).toOkioPath())
                when (load) {
                    is PrefabLoadResult.Failure ->
                        sender.error("Failed to read prefab $namespace:$path:\n${load.error.message}")

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
}
