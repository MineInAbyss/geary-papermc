package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent

@Serializable
@SerialName("geary:on.item_break")
class OnItemBreak

class ItemBreakBridge: Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerItemBreakEvent.onItemBreak() {
        val heldItem = player.inventory.toGeary()?.itemInMainHand ?: return
        EventHelpers.runSkill<OnItemBreak>(heldItem)
    }
}
