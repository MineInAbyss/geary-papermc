package com.mineinabyss.geary.papermc.features.items.recipes

import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack

/**
 * Drives CraftingTableRecipeListener the way a crafting grid does.
 * Placing items fires the prepare event over the result vanilla would show, clicks fire on the result slot
 */
internal class CraftingTable(manager: RecipeManager, val player: TestPlayer = TestPlayer(), size: Int = 9) {
    var listener = CraftingTableRecipeListener(manager)
        private set
    val inventory = TestCraftingInventory(size)
    val cursor = Cursor()
    val view = craftingView(player, inventory, cursor)

    val matrix: List<ItemStack?> get() = inventory.matrix.toList()

    fun place(vararg items: ItemStack?): ItemStack? {
        inventory.matrix = Array(inventory.size) { items.getOrNull(it) }
        return prepare()
    }

    fun prepare(): ItemStack? {
        inventory.result = inventory.recipe?.result?.takeUnless { it.isEmpty }
        listener.run { PrepareItemCraftEvent(inventory, view, false).onPrepareCraft() }
        return inventory.result
    }

    fun click(
        action: InventoryAction = InventoryAction.PICKUP_ALL,
        click: ClickType = if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) ClickType.SHIFT_LEFT else ClickType.LEFT,
        hotbarButton: Int = -1,
    ): InventoryClickEvent = resultClick(view, action, click, hotbarButton).also { listener.run { it.onClickResult() } }

    fun shiftClick() = click(InventoryAction.MOVE_TO_OTHER_INVENTORY)

    /** Keeps the grid open while the listener is replaced, as a reload does */
    fun reload(manager: RecipeManager) {
        listener = CraftingTableRecipeListener(manager)
    }
}
