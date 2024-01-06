package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

@Serializable
@SerialName("geary:on.item_consumed")
class OnItemConsume

@Serializable
@SerialName("geary:consumer")
sealed class Consumer

class ItemConsumeBridge: Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerItemConsumeEvent.onConsume() {
        val heldItem = player.inventory.toGeary()?.itemInMainHand ?: return
        EventHelpers.runSkill<OnItemConsume>(heldItem) {
            addRelation<Consumer>(player.toGeary())
        }
    }
}
