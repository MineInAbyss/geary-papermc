package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info

fun Command.debug() {
    "debug" {
        "inventory" {
            playerAction {
                repeat(64) {
                    val entities = player.toGeary()
                        .get<PlayerItemCache<*>>()
                        ?.getEntities() ?: return@playerAction

                    player.info(
                        entities
                            .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                            .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" }
                    )
                }
            }
        }
    }
}
