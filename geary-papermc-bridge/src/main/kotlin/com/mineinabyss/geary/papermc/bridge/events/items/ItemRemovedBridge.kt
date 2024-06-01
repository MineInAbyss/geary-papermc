package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.PlayerInventory

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
    fun InventoryClickEvent.emitOnItemDrop() {
        // InventoryCreativeEvent handles CREATIVE-mode
        if (click == ClickType.CREATIVE) return
        // Skip any clicks not outside the screen or players inventory
        if (clickedInventory != null && clickedInventory !is PlayerInventory) return

        val droppedItem = when (action) {
            // dropped by clicking outside of inventory
            InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ONE_CURSOR ->
                whoClicked.inventory.toGeary()?.itemOnCursor ?: return
            // dropped by pressing Q while hovering a slot
            InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT ->
                whoClicked.inventory.toGeary()?.get(slot) ?: return
            else -> return
        }
        droppedItem.emit<OnItemDrop>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerDropItemEvent.emitOnItemDrop() {
        // The dropped itemstack will never be found in the inventory
        // If one drops a single item, it would look for an itemstack with size 1
        // if one drops the entire stack then it would not exist in the inventory
        //val droppedItem = player.inventory.toGeary()?.find(itemDrop.itemStack) ?: return
        //droppedItem.emit<OnItemDrop>()
    }
}
