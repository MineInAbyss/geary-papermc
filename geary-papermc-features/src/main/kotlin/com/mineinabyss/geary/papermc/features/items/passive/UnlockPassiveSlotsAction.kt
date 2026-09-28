package com.mineinabyss.geary.papermc.features.items.passive

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

/** Unlocks passive slots for the player this runs on, adding [amount] to their count, or setting it when [set] is true */
@Serializable
@SerialName("geary:unlock_passive_slots")
class UnlockPassiveSlotsAction(
    val amount: Int,
    val set: Boolean = false,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return
        if (set) PassiveSlots.setUnlocked(player, amount) else PassiveSlots.unlock(player, amount)
    }
}
