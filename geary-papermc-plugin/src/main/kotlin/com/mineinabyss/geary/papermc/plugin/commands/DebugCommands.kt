package com.mineinabyss.geary.papermc.plugin.commands

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.engine.archetypes.ArchetypeQueryManager
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.get
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackContent
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.schema_generator.GearySchema
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.spawning.statistics.EntityStatistics
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import kotlin.io.path.div

internal fun IdoCommand.debug() = "debug" {
    requiresPermission("geary.admin.debug")
    "generateschema" {
        executes {
            GearySchema(
                gearyPaper.plugin.dataPath / "schema.ts", gearyPaper.worldManager.global.getAddon(SerializableComponents)
            ).generate()
        }
    }
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
            val world = gearyPaper.worldManager.global
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
    "dumpStats" {
        executes {
            sender.info("Dumping stats...")
            DI.get<EntityStatistics>().dumpData().invokeOnCompletion {
                sender.info("Stat dump finished")
            }
        }
    }
    "resetStats" {
        executes {
            DI.get<EntityStatistics>().reset()
        }
    }
    "async" {
        "read" {
            playerExecutes {
                gearyPaper.plugin.launch(gearyPaper.plugin.asyncDispatcher) {
                    player.toGeary().get<PlayerItemCache<*>>()
                }
            }
        }
        "write" {
            playerExecutes {

                gearyPaper.plugin.launch(gearyPaper.plugin.asyncDispatcher) {
                    player.toGeary().set(DebugComponent())
                }
            }
        }
    }
    "getNearbyDBEntries" {
        playerExecutes {
            gearyPaper.features.getOrNull<SpawningFeature>()?.dumpDB(player.location, player)
        }
    }
    "clearDB" {
        playerExecutes {
            gearyPaper.features.getOrNull<SpawningFeature>()?.clearDB(player.world)
            sender.success("Cleared spawn locations from the database.")
        }
    }
}
