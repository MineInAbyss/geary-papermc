package com.mineinabyss.geary.papermc.features.items.consumable

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:requires_consumable")
class RequiresConsumable(
    val type: SerializableItemStack,
    val minAmount: Int = 1,
)

//@AutoScan
//fun GearyModule.createRequiresConsumableCondition() = listener(
//    object : ListenerQuery() {
//        val player by get<Player>()
//        val condition by source.get<RequiresConsumable>()
//    }
//).check {
//    val matchedItem = player.inventory.firstOrNull { condition.type.matches(it) } ?: return@check false
//    matchedItem.amount >= condition.minAmount
//}
//
