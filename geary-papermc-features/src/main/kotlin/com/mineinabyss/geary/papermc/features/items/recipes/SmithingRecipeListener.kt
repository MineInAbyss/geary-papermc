package com.mineinabyss.geary.papermc.features.items.recipes

import org.bukkit.Keyed
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.SmithingInventory

internal class SmithingRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareSmithingEvent.onPrepareSmithing() {
        val player = view.player as? Player ?: return
        if (!canSmith(player, inventory)) return run { result = null }

        val key = (inventory.recipe as? Keyed)?.key ?: return
        val custom = recipes.getRecipe(key) as? SmithingCustomRecipe ?: return
        val assembled = result ?: return
        if (custom.preservesComponents) {
            result = custom.resultFor(assembled, inventory.inputTemplate, inventory.inputEquipment, inventory.inputMineral)
        }
    }

    // A result stays in its slot until an input changes, so it can outlast a reload or the player losing a permission
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClickSmithingResult() {
        if (slotType != InventoryType.SlotType.RESULT) return
        val inventory = view.topInventory as? SmithingInventory ?: return
        val player = whoClicked as? Player ?: return
        if (!canSmith(player, inventory)) isCancelled = true
    }

    private fun canSmith(player: Player, inventory: SmithingInventory): Boolean {
        val key = (inventory.recipe as? Keyed)?.key ?: return true
        if (!recipes.isRegistered(key)) return false
        if (recipes.isForeign(key)) return true

        val custom = recipes.getRecipe(key) as? SmithingCustomRecipe
        return custom?.hasPermission(player) != false && listOf(inventory.inputTemplate, inventory.inputEquipment, inventory.inputMineral).all { recipes.isAllowedIn(custom, it) }
    }
}
