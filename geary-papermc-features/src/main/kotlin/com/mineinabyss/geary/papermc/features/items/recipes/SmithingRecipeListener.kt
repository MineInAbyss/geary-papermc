package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Keyed
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.SmithingInventory
import org.bukkit.inventory.SmithingTrimRecipe

internal class SmithingRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareSmithingEvent.onPrepareSmithing() {
        val player = view.player as? Player ?: return
        if (!canSmith(player, inventory)) return run { result = null }

        if (inventory.recipe is SmithingTrimRecipe && inventory.inputEquipment?.hasData(DataComponentTypes.EQUIPPABLE) != true) return run { result = null }

        val key = (inventory.recipe as? Keyed)?.key ?: return
        val custom = recipes.getRecipe(key) as? SmithingCustomRecipe ?: return
        if (!custom.canClear(inventory.inputEquipment)) return run { result = null }
        val assembled = result ?: return
        if (custom.inheritsBase || custom.preservesComponents)
            result = custom.resultFor(assembled, inventory.inputTemplate, inventory.inputEquipment, inventory.inputMineral)
    }

    // A result stays in its slot until an input changes, so it can outlast a reload or the player losing a permission
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClickSmithingResult() {
        if (slotType != InventoryType.SlotType.RESULT) return
        val inventory = view.topInventory as? SmithingInventory ?: return
        val player = whoClicked as? Player ?: return
        if (!canSmith(player, inventory)) isCancelled = true
    }

    // Prefab items share a base material, so vanilla cannot tell which slot an item belongs in
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onSmithingSlotClick() {
        val inventory = view.topInventory as? SmithingInventory ?: return
        if (clickedInventory == inventory) {
            if (rawSlot !in TEMPLATE_SLOT..ADDITION_SLOT) return
            val placed = when (click) {
                ClickType.NUMBER_KEY -> (whoClicked as? Player)?.inventory?.getItem(hotbarButton)
                ClickType.SWAP_OFFHAND -> (whoClicked as? Player)?.inventory?.itemInOffHand
                else -> cursor
            } ?: return

            val target = recipes.smithingSlot(placed) ?: return
            if (target != rawSlot) isCancelled = true
            return
        }
        if (!isShiftClick || clickedInventory == null) return

        val moving = currentItem?.takeUnless { it.isEmpty } ?: return
        val target = recipes.smithingSlot(moving) ?: return
        isCancelled = true
        val existing = inventory.getItem(target)

        when {
            existing == null || existing.isEmpty -> {
                inventory.setItem(target, moving.clone())
                currentItem = null
            }
            existing.isSimilar(moving) -> {
                val moved = minOf(moving.amount, existing.maxStackSize - existing.amount)
                if (moved <= 0) return
                existing.amount += moved
                inventory.setItem(target, existing)
                currentItem = moving.takeIf { it.amount > moved }?.apply { amount -= moved }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryDragEvent.onSmithingDrag() {
        if (view.topInventory !is SmithingInventory) return
        val target = recipes.smithingSlot(oldCursor) ?: return

        if (rawSlots.any { it in TEMPLATE_SLOT..ADDITION_SLOT && it != target }) isCancelled = true
    }

    private fun canSmith(player: Player, inventory: SmithingInventory): Boolean {
        val key = (inventory.recipe as? Keyed)?.key ?: return true
        if (!recipes.isRegistered(key)) return false
        if (recipes.isForeign(key)) return true

        val custom = recipes.getRecipe(key) as? SmithingCustomRecipe
        return custom?.hasPermission(player) != false && listOf(inventory.inputTemplate, inventory.inputEquipment, inventory.inputMineral).all { recipes.isAllowedIn(custom, it) }
    }
}

internal const val TEMPLATE_SLOT = 0
internal const val BASE_SLOT = 1
internal const val ADDITION_SLOT = 2
