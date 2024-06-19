package com.mineinabyss.geary.papermc.plugin.startup

import com.mineinabyss.geary.papermc.PaperEngineModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys

fun PaperEngineModule.trackExistingBukkitEntities() {
    val config = config
    val logger = logger
    onPluginEnable {
        if (config.trackEntities) {
            server.worlds.forEach { world ->
                world.entities.forEach entities@{ entity ->
                    gearyMobs.bukkit2Geary.getOrCreate(entity)
                }
            }
        }
        logger.s(
            "Loaded prefabs - Mobs: ${gearyMobs.query.prefabs.getKeys().size}, Blocks: ${gearyBlocks.prefabs.getKeys().size}, Items: ${gearyItems.prefabs.getKeys().size}"
        )
    }
}
