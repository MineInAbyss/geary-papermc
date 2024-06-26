package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error

fun Command.items() {
    "items" {
        "give" {
            val prefabKey by optionArg(options = gearyItems.prefabs.map { it.key.toString() }) {
                parseErrorMessage = { "No such entity: $passed" }
            }
            val amount by intArg { default = 1 }
            playerAction {
                val item = gearyItems.createItem(PrefabKey.of(prefabKey))
                    ?: return@playerAction sender.error("Failed to create $prefabKey")
                item.amount = amount.coerceIn(1, item.maxStackSize)
                player.inventory.addItem(item)
            }
        }
    }
}
