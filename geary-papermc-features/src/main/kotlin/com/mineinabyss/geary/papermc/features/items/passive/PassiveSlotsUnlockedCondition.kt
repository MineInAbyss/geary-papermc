package com.mineinabyss.geary.papermc.features.items.passive

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

/** Passes when the player has at least [min] and at most [max] passive slots unlocked */
@Serializable
@SerialName("geary:passive_slots_unlocked")
class PassiveSlotsUnlockedCondition(
    val min: Int = 0,
    val max: Int = PassiveSlots.COUNT,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = entity?.get<Player>() ?: return false
        val unlocked = PassiveSlots.get(player)?.unlockedSlots ?: return false
        return unlocked in min..max
    }
}
