package com.mineinabyss.geary.papermc.features.items.consumables

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:consume_item")
class ConsumeItemFromInventoryAction(
    val type: SerializableItemStack,
    val amount: Int = 1,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return
        val matchedItem = player.inventory.filterNotNull().firstOrNull { type.matches(it) }
            ?: player.inventory.itemInOffHand.takeIf { type.matches(it) } ?: return
        matchedItem.amount -= amount
    }
}
