package com.mineinabyss.geary.papermc.features.items.recipes

import com.destroystokyo.paper.event.inventory.PrepareResultEvent
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.StonecutterView

internal class StonecutterRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun PlayerStonecutterRecipeSelectEvent.onSelectRecipe() {
        if (!canCut(player, stonecuttingRecipe.key, stonecutterInventory.inputItem)) isCancelled = true
    }

    // The selected recipe is kept when the input is swapped for another stack of the same material, and no select event fires
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareResultEvent.onPrepareStonecutter() {
        val view = view as? StonecutterView ?: return
        if (!canCutSelected(view)) result = null
    }

    // Taking a result assembles the next one after its prepare event, bringing back a result that event cleared
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClickStonecutterResult() {
        val view = view as? StonecutterView ?: return
        if (slotType == InventoryType.SlotType.RESULT && !canCutSelected(view)) isCancelled = true
    }

    private fun canCutSelected(view: StonecutterView): Boolean {
        val player = view.player as? Player ?: return true
        val selected = view.recipes.getOrNull(view.selectedRecipeIndex) ?: return true
        return canCut(player, selected.key, view.topInventory.inputItem)
    }

    // An open stonecutter keeps the recipes it listed for its input, which a reload can remove
    private fun canCut(player: Player, key: NamespacedKey, input: ItemStack?): Boolean {
        if (!recipes.isRegistered(key)) return false
        if (recipes.isForeign(key)) return true
        val custom = recipes.getRecipe(key)
        return !(custom != null && (!custom.hasPermission(player) || custom.ingredients.none { it.matches(input) })) && recipes.isAllowedIn(custom, input)
    }
}
