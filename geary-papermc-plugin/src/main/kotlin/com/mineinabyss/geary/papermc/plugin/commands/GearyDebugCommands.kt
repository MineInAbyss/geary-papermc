package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.messaging.info
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun IdoRootCommand.debug() {
    "debug" {
        "inventory" {
            fun debugPlayer(sender: CommandSender, player: Player) {
                repeat(64) {
                    val entities = player.toGeary()
                        .get<PlayerItemCache<*>>()
                        ?.getEntities() ?: return

                    sender.info(
                        entities
                            .mapIndexedNotNull { slot, entity -> entity?.getAll()?.map { it::class }?.to(slot) }
                            .joinToString(separator = "\n") { (components, slot) -> "$slot: $components" }
                    )
                }
            }
            playerExecutes {
                repeat(64) {
                    debugPlayer(sender, player)
                }
            }
            val player by ArgumentTypes.player().suggests {
                suggest(Bukkit.getOnlinePlayers().map { it.name })
            }
            playerExecutes {
                val player = player()!!.resolve(context.source).firstOrNull() ?: executor as? Player ?: return@playerExecutes
                debugPlayer(sender, player)
            }
        }
    }
}
