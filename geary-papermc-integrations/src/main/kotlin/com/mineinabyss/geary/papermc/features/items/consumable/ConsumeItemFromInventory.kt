package com.mineinabyss.geary.papermc.features.items.consumable

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("looty:consume_item")
class ConsumeItemFromInventory(
    val type: SerializableItemStack,
    val amount: Int = 1,
)

//@AutoScan
//fun GearyModule.createConsumeItemAction() = listener(
//    object : ListenerQuery() {
//        val player by get<Player>()
//        val action by source.get<ConsumeItemFromInventory>()
//    }
//).exec {
//    val matchedItem = player.inventory.firstOrNull { action.type.matches(it) } ?: return@exec
//    matchedItem.amount -= action.amount
//}

