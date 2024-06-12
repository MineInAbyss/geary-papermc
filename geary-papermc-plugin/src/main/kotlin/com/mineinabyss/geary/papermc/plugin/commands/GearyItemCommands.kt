package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.plugin.commands.GearyCommands.filterPrefabs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.sun.jdi.connect.Connector.IntegerArgument

fun IdoRootCommand.items() {
    "items" {
        "give" {
            val prefabKey by PrefabKeyArgument().suggests {
                suggest(gearyItems.prefabs.getKeys().filterPrefabs(context.input.substringAfterLast(" ")).toList())
            }
            val amount by IntegerArgumentType.integer(1)
            playerExecutes {
                val item = gearyItems.createItem(prefabKey()!!) ?: run {
                    sender.error("Failed to create $prefabKey")
                    return@playerExecutes
                }
                item.amount = amount()
                player.inventory.addItem(item)
            }
        }
    }
}
