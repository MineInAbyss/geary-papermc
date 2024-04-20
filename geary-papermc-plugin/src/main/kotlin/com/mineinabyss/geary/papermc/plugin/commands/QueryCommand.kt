package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.PlayerAction
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Entity

fun Command.mobsQuery() {
    commandGroup {
        val query by stringArg()
        val radius by intArg { default = 0 }

        fun PlayerAction.removeOrInfo(isInfo: Boolean) {
            val worlds = gearyPaper.plugin.server.worlds
            var entityCount = 0
            val entities = mutableSetOf<Entity>()
            val types = query.split("+")

            for (world in worlds) for (entity in world.entities) {
                val geary = entity.toGearyOrNull() ?: continue
                // Only select entities that are instanced from a gearyMobs registered prefab
                if (!GearyMobPrefabQuery.isMob(geary)) continue

                if (types.any { type ->
                        when (type) {
                            "custom" -> true
                            else -> {
                                val prefab = runCatching { PrefabKey.of(type).toEntityOrNull() }.getOrNull()
                                    ?: this@commandGroup.stopCommand("No such prefab or selector $type")
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
                ${if (radius <= 0) "in all loaded chunks." else "in a radius of $radius blocks."}
                """.trimIndent().replace("\n", " ")
            )
            if (isInfo) {
                val categories = entities
                    .asSequence()
                    .flatMap { it.toGeary().prefabs }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .toList()

                if (categories.isNotEmpty()) sender.info(
                    // Print mobs
                    buildString {
                        val mobs = categories.filterTo(mutableSetOf()) { GearyMobPrefabQuery.isMob(it.key) }
                        if (mobs.isNotEmpty()) appendLine("Mobs:")
                        mobs.forEach { (type, amount) ->
                            val prefabName = type.get<PrefabKey>()?.toString() ?: type.toString()
                            appendLine("<gray>${prefabName}</gray>: $amount")
                        }
                        val otherPrefabs = categories - mobs
                        if (otherPrefabs.isNotEmpty()) appendLine("Other prefabs:")
                        categories.filter { !GearyMobPrefabQuery.isMob(it.key) }.forEach { (type, amount) ->
                            val prefabName = type.get<PrefabKey>()?.toString() ?: type.toString()
                            appendLine("<dark_gray>${prefabName}</dark_gray>: $amount")
                        }
                    }
                )
            }
        }

        ("remove" / "rm")(desc = "Removes mobs")?.playerAction {
            removeOrInfo(false)
        }

        ("info" / "i")(desc = "Lists how many mobs are around you")?.playerAction {
            removeOrInfo(true)
        }
    }
}
