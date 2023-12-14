package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.commands.locate
import com.mineinabyss.geary.papermc.plugin.commands.query
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
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
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

internal class GearyCommands : IdofrontCommandExecutor(), TabCompleter {
    private val plugin get() = gearyPaper.plugin
    private val prefabLoader get() = prefabs.loader
    private val prefabManager get() = prefabs.manager
    private val engine get() = geary.engine

    override val commands = commands(plugin) {
        "geary" {
            "stats" {
                action {
                    val tempEntity = entity()

                    sender.info(
                        """
                        |Archetype count: ${archetypes.queryManager.archetypeCount}
                        |Next entity ID: ${tempEntity.id}
                        |""".trimMargin()
                    )

                    tempEntity.removeEntity()
                }
            }
            "debug" {
                "inventory" {
                    playerAction {
                        repeat(64) {
                            val entities =
                                player.toGeary().get<PlayerItemCache<*>>()?.getEntities() ?: return@playerAction
                            player.info(entities
                                .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                                .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" })
                        }
                    }
                }
            }
            ("items") {
                ("give") {
                    val prefabKey by optionArg(options = gearyItems.prefabs.run { toList { it.key.toString() } }) {
                        parseErrorMessage = { "No such entity: $passed" }
                    }
                    val amount by intArg { default = 1 }
                    playerAction {
                        val item = gearyItems.createItem(PrefabKey.of(prefabKey)) ?: run {
                            sender.error("Failed to spawn $prefabKey")
                            return@playerAction
                        }
                        item.amount = amount.coerceIn(1, 64)
                        player.inventory.addItem(item)
                    }
                }
            }
            ("mobs" / "m") {
                ("spawn" / "s") {
                    val mobKey by optionArg(options = gearyMobs.prefabs.run { toList { it.key.toString() } }) {
                        parseErrorMessage = { "No such entity: $passed" }
                    }
                    val numOfSpawns by intArg {
                        name = "number of spawns"
                        default = 1
                    }

                    playerAction {
                        val cappedSpawns = numOfSpawns//.coerceAtMost(mobzySpawning.config.maxCommandSpawns)
                        val key = PrefabKey.of(mobKey)

                        repeat(cappedSpawns) {
                            player.location.spawnFromPrefab(key).onFailure {
                                sender.error("Failed to spawn $key")
                                it.printStackTrace()
                            }
                        }
                    }
                }
                locate()
                query()
            }
            "reload" {
                action {
                    gearyPaper.configHolder.reload()
                }
            }
            "prefab" {
                "reload" {
                    val prefab by stringArg()
                    action {
                        engine.launch {
                            runCatching { prefabLoader.reread(PrefabKey.of(prefab).toEntity()) }
                                .onSuccess { sender.success("Reread prefab $prefab") }
                                .onFailure { sender.error("Failed to reread prefab $prefab:\n${it.message}") }
                        }
                    }
                }
                "load" {
                    val namespace by stringArg()
                    val path by stringArg()
                    action {
                        engine.launch {
                            // Ensure not already registered
                            if (prefabManager[PrefabKey.of(namespace, Path(path).nameWithoutExtension)] != null) {
                                sender.error("Prefab $namespace:$path already exists")
                                return@launch
                            }

                            // Try to load from file
                            prefabLoader.loadFromPath(
                                namespace,
                                plugin.dataFolder.resolve(namespace).resolve(path).toOkioPath()
                            ).onSuccess {
                                it.inheritPrefabs()
                                sender.success("Read prefab $namespace:$path")
                            }.onFailure { sender.error("Failed to read prefab $namespace:$path:\n${it.message}") }
                        }
                    }
                }
            }
        }
    }

    private fun getGearyDependants(): List<Plugin> =
        Bukkit.getServer().pluginManager.plugins.filter { "Geary" in it.description.depend }

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

            "prefab" -> when (if (args.size == 2) return listOf("load", "reload") else args[1]) {
                "reload" -> return prefabManager.keys.filter {
                    val arg = args[2].lowercase()
                    it.key.startsWith(arg) || it.full.startsWith(arg)
                }.map { it.toString() }

                "load" -> return when (args.size) {
                    3 -> plugin.dataFolder.listFiles()?.filter {
                        it.isDirectory && it.name.startsWith(args[2].lowercase())
                    }?.map { it.name } ?: listOf()

                    4 -> plugin.dataFolder.resolve(args[2]).walkTopDown().toList().filter {
                        it.isFile && it.extension == "yml" && it.nameWithoutExtension.startsWith(args[3].lowercase())
                                && "${args[2]}:${it.nameWithoutExtension}" !in prefabManager.keys.map(PrefabKey::full)
                    }.map {
                        it.absolutePath.split(plugin.dataFolder.absolutePath + "\\" + args[2] + "\\")[1]
                            .replace("\\", "/")
                    }

                    else -> return listOf()
                }

            }

            else -> return listOf()
        }
        return emptyList()
    }

    private val mobs: List<String> by lazy {
        buildList {
            addAll(listOf("custom"))
            addAll(gearyMobs.prefabs.getKeys().map { it.toString() })
        }
    }
}
