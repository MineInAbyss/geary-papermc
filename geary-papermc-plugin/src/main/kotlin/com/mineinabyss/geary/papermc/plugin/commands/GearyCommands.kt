package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery.Companion.getKeyStrings
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.inheritPrefabs
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.typealiases.BukkitEntity
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter {
    private val plugin get() = gearyPaper.plugin
    private val prefabManager get() = prefabs.manager

    override val commands = commands(plugin) {
        "geary" {
            stats()
            debug()
            items()
            mobs()
            prefabs()
            reload()
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        fun Collection<PrefabKey>.filterPrefabs(arg: String) =
            filter { it.key.startsWith(arg) || it.full.startsWith(arg) }.map { it.toString() }.take(20)

        when (if (args.size == 1) return listOf(
            "mobs",
            "items",
            "prefab",
            "stats",
            "reload"
        ).filter { it.startsWith(args[0]) } else args[0]) {
            "mobs", "m" -> when (if (args.size == 2) return listOf("spawn", "remove", "locate", "info") else args[1]) {
                "spawn", "s" -> if (args.size == 3) {
                    return gearyMobs.prefabs.getKeys().filterPrefabs(args[2]).toList()
                } else if (args.size == 4) {
                    val min = args[3].toIntOrNull()?.coerceAtLeast(1) ?: 1
                    return (min - 1 until min + 100).map { it.toString() }
                }

                "remove", "rm", "info", "i", "locate" -> if (args.size == 3) {
                    val query = args[2].lowercase()
                    val parts = query.split("+")
                    val withoutLast = query.substringBeforeLast("+", missingDelimiterValue = "").let {
                        if (parts.size > 1) "$it+" else it
                    }
                    return mobs.asSequence().filter {
                        it !in parts && (it.startsWith(parts.last()) ||
                                it.substringAfter(":").startsWith(parts.last()))
                    }.take(20).map { "$withoutLast$it" }.toList()
                }
            }

            "items" -> when (if (args.size == 2) return listOf("give") else args[1]) {
                "give" -> if (args.size == 3) {
                    return gearyItems.prefabs.getKeys().filterPrefabs(args[2]).toList()
                }
            }

            "prefab" -> {
                if (!sender.hasPermission("geary.prefab")) return emptyList()
                when (if (args.size == 2) return listOf("load", "reload") else args[1]) {
                    "reload" -> return prefabManager.keys.filter {
                        val arg = args[2].lowercase()
                        it.key.startsWith(arg) || it.full.startsWith(arg)
                    }.map { it.toString() }

                    "load" -> return when (args.size) {
                        3 -> plugin.dataFolder.listFiles()?.filter {
                            it.isDirectory && it.name.startsWith(args[2].lowercase())
                        }?.map { it.name } ?: listOf()

                        4 -> {
                            plugin.dataFolder.resolve(args[2])
                                .walk()
                                .filter {
                                    it.extension == "yml"
                                            && it.nameWithoutExtension.startsWith(args[3].lowercase())
                                            && prefabManager[PrefabKey.of(args[2], it.nameWithoutExtension)] == null
                                }.map {
                                    it.relativeTo(plugin.dataFolder.resolve(args[2])).toString()
                                }
                                .toList()
                        }

                        else -> return listOf()
                    }
                }
            }

            else -> return listOf()
        }
        return emptyList()
    }

    private val mobs: List<String> by lazy {
        buildList {
            addAll(listOf("custom"))
            addAll(gearyMobs.prefabs.getKeyStrings())
        }
    }
}
