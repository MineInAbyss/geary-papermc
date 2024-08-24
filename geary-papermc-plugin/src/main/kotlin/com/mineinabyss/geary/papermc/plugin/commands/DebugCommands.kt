package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackContent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.info
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

internal fun IdoCommand.debug() = "debug" {
    requiresPermission("geary.admin.debug")
    "inventory" {
        playerExecutes {
            repeat(64) {
                val entities = player.toGeary()
                    .get<PlayerItemCache<*>>()
                    ?.getEntities() ?: return@playerExecutes

                player.info(
                    entities
                        .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                        .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" }
                )
            }
        }
    }
    "resourcepack_items" {
        playerExecutes {
            val items = gearyItems.prefabs.mapNotNull {
                it.key.toEntityOrNull()?.has<ResourcePackContent>()?.takeIf { it }
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
}
