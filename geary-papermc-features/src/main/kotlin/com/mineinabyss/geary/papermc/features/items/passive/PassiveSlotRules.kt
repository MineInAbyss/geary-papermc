package com.mineinabyss.geary.papermc.features.items.passive

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.components.PassiveItem
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveInventory
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

/** Only items marked [PassiveItem] may enter a passive slot, locked slots reject everything*/
object PassiveSlotRules {
    fun allowed(player: Player, item: ItemStack?): Boolean {
        if (item == null || item.isEmpty) return true
        return player.world.toGeary().itemEntityContext { item.toGearyOrNull()?.has<PassiveItem>() == true }
    }

    /** Cancels [event] when it would move a disallowed item into a passive slot, returning whether passive slots were touched */
    fun handleClick(event: InventoryClickEvent, passive: PassiveInventory): Boolean = with(event) {
        val player = whoClicked as? Player ?: return false
        when {
            clickedInventory === view.topInventory -> {
                if (!passive.isUnlocked(slot)) return cancel()
                val entering = when (action) {
                    InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.SWAP_WITH_CURSOR -> cursor
                    InventoryAction.HOTBAR_SWAP -> player.inventory.let { if (hotbarButton == -1) it.itemInOffHand else it.getItem(hotbarButton) }
                    else -> null
                }
                if (!allowed(player, entering)) return cancel()
            }
            // Shift clicks land in the first free top slot, fillers keep locked slots occupied
            action == InventoryAction.MOVE_TO_OTHER_INVENTORY -> if (!allowed(player, currentItem)) return cancel()
            else -> return false
        }
        validateLater(player, passive)
        return true
    }

    fun handleDrag(event: InventoryDragEvent, passive: PassiveInventory): Boolean = with(event) {
        val player = whoClicked as? Player ?: return false
        val passiveSlots = rawSlots.filter { it < view.topInventory.size }
        if (passiveSlots.isEmpty()) return false
        if (passiveSlots.any { !passive.isUnlocked(it) } || !allowed(player, oldCursor)) return cancel()
        validateLater(player, passive)
        return true
    }

    /** Backstop for click actions the pre-checks cannot reason about, returning anything disallowed to the player */
    private fun validateLater(player: Player, passive: PassiveInventory) {
        gearyPaper.launch {
            delay(1.ticks)
            repeat(PassiveSlots.COUNT) { slot ->
                val item = passive.inventory.getItem(slot) ?: return@repeat
                if (PassiveSlots.isFiller(item) || (passive.isUnlocked(slot) && allowed(player, item))) return@repeat
                passive.inventory.setItem(slot, null)
                player.inventory.addItem(item).values.forEach { player.world.dropItemNaturally(player.location, it) }
            }
        }
    }

    private fun Cancellable.cancel(): Boolean {
        isCancelled = true
        return false
    }
}
