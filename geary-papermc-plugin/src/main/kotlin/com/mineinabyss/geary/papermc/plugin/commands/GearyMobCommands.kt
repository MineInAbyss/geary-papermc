package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.commands.GearyCommands.filterPrefabs
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeyStrings
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.brigadier.IdoPlayerCommandContext
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.SharedSuggestionProvider.suggest
import org.bukkit.Bukkit
import org.bukkit.entity.Entity

fun IdoRootCommand.mobs() =
    ("mobs") {
        ("spawn") {
            val mobKey by PrefabKeyArgument().suggests {
                suggest(gearyMobs.query.spawnablePrefabs.getKeys().filterPrefabs(context.input.substringAfterLast(" ")).toList())
            }
            val numOfSpawns by IntegerArgumentType.integer(1)

            playerExecutes {
                val (mobKey, cappedSpawns) = mobKey()!! to numOfSpawns()!!

                repeat(cappedSpawns) {
                    player.location.spawnFromPrefab(mobKey).onFailure {
                        sender.error("Failed to spawn $mobKey")
                        it.printStackTrace()
                    }
                }
            }
        }

        "locate" {
            val mobKey by PrefabKeyArgument().suggests {
                suggest(gearyMobs.query.spawnablePrefabs.getKeys().filterPrefabs(context.input.substringAfterLast(" ")).toList())
            }
            val radius by IntegerArgumentType.integer()
            playerExecutes {
                val (key, radius) = mobKey()!! to radius()!!
                if (radius <= 0) {
                    Bukkit.getWorlds().forEach { world ->
                        world.entities.filter { it.toGeary().deepInstanceOf(key.toEntity()) }.forEach { entity ->
                            val (x, y, z) = entity.location.toBlockLocation().toVector()
                            player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z</aqua> in ${entity.world.name}")
                        }
                    }
                } else {
                    player.location.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())
                        .filter { it.toGeary().deepInstanceOf(key.toEntity()) }.forEach { entity ->
                            val (x, y, z) = entity.location.toBlockLocation().toVector()
                            player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z")
                        }
                }
            }
        }

        fun IdoPlayerCommandContext.removeOrInfo(query: String, radius: Int, isInfo: Boolean) {
            val worlds = gearyPaper.plugin.server.worlds
            var entityCount = 0
            val entities = mutableSetOf<Entity>()
            val types = query.split("+")

            for (world in worlds) for (entity in world.entities) {
                val geary = entity.toGearyOrNull() ?: continue
                // Only select entities that are instanced from a gearyMobs registered prefab
                if (!gearyMobs.query.isMob(geary)) continue

                if (types.any { type ->
                        when (type) {
                            "custom" -> true
                            else -> {
                                val prefab = runCatching { PrefabKey.of(type).toEntityOrNull() }.getOrNull() ?: return
                                geary.deepInstanceOf(prefab)
                            }
                        }
                    }) {
                    val playerLoc = player.location
                    if (radius <= 0 || entity.world == playerLoc.world && entity.location.distance(playerLoc) < radius) {
                        entityCount++
                        if (isInfo) entities += entity
                        else entity.remove()
                    }
                }
            }

            sender.success(
                """
                ${if (isInfo) "There are" else "Removed"}
                <b>$entityCount</b> entities matching your query
                ${if (radius <= 0) "in loaded chunks." else "in a radius of $radius blocks."}
                """.trimIndent().replace("\n", " ")
            )
            if (isInfo) {
                val mobs = entities
                    .asSequence()
                    .flatMap { it.toGeary().prefabs }
                    .groupingBy { it }
                    .eachCount()
                    .filter { gearyMobs.query.isMobPrefab(it.key) }
                    .entries
                    .sortedByDescending { it.value }
                    .toList()

                if (mobs.isNotEmpty()) sender.info(
                    mobs.joinToString(separator = "\n") { (type, amount) ->
                        val prefabName = type.get<PrefabKey>()?.toString() ?: type.toString()
                        "<gray>${prefabName}</gray>: $amount"
                    }
                )
            }
        }

        ("remove") {
            val query by StringArgumentType.string().suggests {
                val query = context.input.lowercase()
                val parts = query.split("+")
                val withoutLast = query.substringBeforeLast("+", missingDelimiterValue = "").let {
                    if (parts.size > 1) "$it+" else it
                }

                suggest(GearyCommands.mobs.asSequence().filter {
                    it !in parts && (it.startsWith(parts.last()) ||
                            it.substringAfter(":").startsWith(parts.last()))
                }.take(20).map { "$withoutLast$it" }.toList())
            }
            val radius by IntegerArgumentType.integer()
            playerExecutes {
                removeOrInfo(query()!!, radius()!!, false)
            }
        }

        "info"{
            val query by StringArgumentType.string().suggests {
                val query = context.input.lowercase()
                val parts = query.split("+")
                val withoutLast = query.substringBeforeLast("+", missingDelimiterValue = "").let {
                    if (parts.size > 1) "$it+" else it
                }

                suggest(GearyCommands.mobs.asSequence().filter {
                    it !in parts && (it.startsWith(parts.last()) ||
                            it.substringAfter(":").startsWith(parts.last()))
                }.take(20).map { "$withoutLast$it" }.toList())
            }
            val radius by IntegerArgumentType.integer()
            playerExecutes {
                removeOrInfo(query()!!, radius()!!, false)
            }
        }
    }
