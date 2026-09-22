package com.mineinabyss.geary.papermc.plugin.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.engine.archetypes.ArchetypeQueryManager
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.plugin.Plugin

val DebugFeature = module("debug") { }.mainCommand {
    "debug" {
        permission = "geary.admin.debug"
        "inventory" {
            executes.asPlayer {
                repeat(64) {
                    val entities = player.toGeary()
                        .get<PlayerItemCache<*>>()
                        ?.getEntities() ?: fail("No item cache tracked for ${player.name}")

                    player.info(
                        entities
                            .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                            .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" }
                    )
                }
            }
        }
        "stats" {
            executes {
                val world = get<Geary>()
                val tempEntity = world.entity()

                sender.info(
                    """
                        |Archetype count: ${get<ArchetypeQueryManager>().archetypeCount}
                        |Next entity ID: ${tempEntity.id}
                        |""".trimMargin()
                )

                tempEntity.removeEntity()
            }
        }
        "async" {
            "read" {
                executes.asPlayer {
                    val plugin = get<Plugin>()
                    plugin.launch(plugin.asyncDispatcher) {
                        runCatching { player.toGeary().get<PlayerItemCache<*>>() }
                            .onSuccess { sender.success("Async read succeeded, cache ${if (it == null) "missing" else "present"}") }
                            .onFailure { sender.error("Async read failed:\n${it.message}") }
                    }
                }
            }
            "write" {
                executes.asPlayer {
                    val plugin = get<Plugin>()
                    plugin.launch(plugin.asyncDispatcher) {
                        runCatching { player.toGeary().set(DebugComponent()) }
                            .onSuccess { sender.success("Async write succeeded") }
                            .onFailure { sender.error("Async write failed:\n${it.message}") }
                    }
                }
            }
        }
    }
}