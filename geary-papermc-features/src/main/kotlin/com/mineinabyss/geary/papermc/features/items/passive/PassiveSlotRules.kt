package com.mineinabyss.geary.papermc.features.items.passive

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.components.PassiveItem
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveInventory
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
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
                // The item leaving a bundle is not known before the click resolves, and items put into a bundle skip the passive check
                if (action in bundleActions) return cancel()
                val entering = when (action) {
                    InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.SWAP_WITH_CURSOR -> cursor
                    InventoryAction.HOTBAR_SWAP -> player.inventory.let { if (hotbarButton == -1) it.itemInOffHand else it.getItem(hotbarButton) }
                    else -> null
                }
                if (!allowed(player, entering)) return cancel()
            }
            // Shift clicks land in the first free top slot, which is a locked one when no filler item is configured
            action == InventoryAction.MOVE_TO_OTHER_INVENTORY -> if (!allowed(player, currentItem) || hasOpenLockedSlot(passive)) return cancel()
            // Double clicking gathers matching stacks out of the passive slots too
            action == InventoryAction.COLLECT_TO_CURSOR -> return true
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
            // The owner's data was saved when the inventory went inactive, moving items out now would duplicate them
            if (!PassiveSlots.isActive(passive)) return@launch
            var corrected = false
            repeat(PassiveSlots.COUNT) { slot ->
                val item = passive.inventory.getItem(slot) ?: return@repeat
                if (PassiveSlots.isFiller(item) || (passive.isUnlocked(slot) && allowed(player, item))) return@repeat
                passive.inventory.setItem(slot, null)
                player.inventory.addItem(item).values.forEach { player.world.dropItemNaturally(player.location, it) }
                corrected = true
            }
            if (corrected) PassiveSlots.save(passive)
        }
    }

    private val bundleActions = setOf(
        InventoryAction.PLACE_FROM_BUNDLE,
        InventoryAction.PLACE_ALL_INTO_BUNDLE,
        InventoryAction.PLACE_SOME_INTO_BUNDLE,
    )

    private fun hasOpenLockedSlot(passive: PassiveInventory) = (0 until PassiveSlots.COUNT).any { slot ->
        !passive.isUnlocked(slot) && passive.inventory.getItem(slot).let { it == null || it.isEmpty }
    }

    private fun Cancellable.cancel(): Boolean {
        isCancelled = true
        return false
    }
}
