package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot

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
        val gearyInventory = player.inventory.toGeary() ?: return
        val heldItem = when (hand) {
            EquipmentSlot.HAND -> gearyInventory.itemInMainHand
            else -> gearyInventory.itemInOffhand
        } ?: return
        heldItem.emit<OnItemConsume>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerItemBreakEvent.emitOnItemBreak() {
        val brokenItem = player.inventory.toGeary()?.find(brokenItem) ?: return
        brokenItem.emit<OnItemBreak>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerDropItemEvent.emitOnItemDrop() {
        itemEntityContext {
            val droppedItem = itemDrop.itemStack.toGearyOrNull() ?: return
            droppedItem.addParent(player.toGeary())
            droppedItem.emit<OnItemDrop>()
        }
    }
}
