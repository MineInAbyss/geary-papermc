package com.mineinabyss.geary.papermc.plugin.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.engine.archetypes.ArchetypeQueryManager
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.get
import com.mineinabyss.geary.papermc.features.resourcepacks.ResourcePackContent
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.info
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.plugin.Plugin

val DebugFeature = feature("debug") {
    mainCommand {
        "debug" {
            permission = "geary.admin.debug"
            "inventory" {
                executes.asPlayer {
                    repeat(64) {
                        val entities = player.toGeary()
                            .get<PlayerItemCache<*>>()
                            ?.getEntities() ?: return@asPlayer

                        player.info(
                            entities
                                .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                                .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" }
                        )
                    }
                }
            }
            "resourcepack_items" {
                executes.asPlayer {
                    val world = player.world.toGeary()
                    val gearyItems = world.getAddon(ItemTracking)
                    val items = gearyItems.prefabs.mapNotNull {
                        world.entityOfOrNull(it.key)?.has<ResourcePackContent>()?.takeIf { it }
                            ?.let { _ -> gearyItems.itemProvider.serializePrefabToItemStack(it.key) }
                    }
                        .chunked(27)
                    val shulkers = items.map { content ->
                        ItemStack.of(Material.SHULKER_BOX).editItemMeta<BlockStateMeta> {
                            blockState = blockState.apply {
                                (this as ShulkerBox).inventory.addItem(*content.toTypedArray())
                                this.update()
                            }
                        }
                    }
                    player.inventory.addItem(*shulkers.toTypedArray())
                }
            }
            "stats" {
                executes {
                    val world = get<Geary>()
                    val tempEntity = world.entity()

                    sender.info(
                        """
                        |Archetype count: ${world.get<ArchetypeQueryManager>().archetypeCount}
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
                            player.toGeary().get<PlayerItemCache<*>>()
                        }
                    }
                }
                "write" {
                    executes.asPlayer {
                        val plugin = get<Plugin>()
                        plugin.launch(plugin.asyncDispatcher) {
                            player.toGeary().set(DebugComponent())
                        }
                    }
                }
            }
        }
    }
}
