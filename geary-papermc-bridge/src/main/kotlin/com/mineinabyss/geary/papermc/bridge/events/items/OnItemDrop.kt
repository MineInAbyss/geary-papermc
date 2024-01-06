package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

@Serializable
@SerialName("geary:on.item_drop")
class OnItemDrop

class ItemDropBridge: Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerDropItemEvent.onItemDrop() {
        val heldItem = player.inventory.toGeary()?.itemInMainHand ?: return
        EventHelpers.runSkill<OnItemDrop>(heldItem)
    }
}
