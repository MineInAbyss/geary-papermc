package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

@Serializable
@SerialName("geary:on_item_consumed")
sealed class OnItemConsume

@Serializable
@SerialName("geary:on_item_break")
sealed class OnItemBreak

@Serializable
@SerialName("geary:on_item_drop")
sealed class OnItemDrop

class ItemRemovedBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerItemConsumeEvent.emitOnConsume() {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemConsume>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerItemBreakEvent.emitOnItemBreak() {
        val brokenItem = player.inventory.toGeary()?.find(brokenItem) ?: return
        brokenItem.emit<OnItemBreak>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerDropItemEvent.emitOnItemDrop() {
        val droppedItem = player.inventory.toGeary()?.find(itemDrop.itemStack) ?: return
        droppedItem.emit<OnItemDrop>()
    }
}
