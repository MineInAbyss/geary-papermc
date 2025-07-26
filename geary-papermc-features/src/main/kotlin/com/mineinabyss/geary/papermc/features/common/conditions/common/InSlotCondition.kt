package com.mineinabyss.geary.papermc.features.common.conditions.common

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:in_slot")
class InSlotCondition(val slots: List<Int>, val item: PrefabKey) : Condition {
    override fun ActionGroupContext.execute(): Boolean = with(entity!!.world){
        val player = entity?.get<Player>()?: return false

        var contains = false
        for (slot in slots) {
            val theItemInSlot = player.inventory.toGeary()?.get(slot)
            contains = contains || theItemInSlot?.prefabs?.contains(item.toEntityOrNull()) == true
        }
        return contains
    }
}