package com.mineinabyss.geary.papermc.features.items.consumables

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:has_consumable")
class HasConsumableCondition(
    val type: SerializableItemStack,
    val minAmount: Int = 1,
): Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val entity = entity ?: return false
        val player = entity.get<Player>() ?: return false
        val matcher = with(entity.world) { ConsumableMatcher.of(type) }
        val matchedItem = player.inventory.firstOrNull { it != null && matcher.matches(it) } ?: return false
        return matchedItem.amount >= minAmount
    }
}
