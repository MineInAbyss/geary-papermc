package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.events.cancelIf
import io.papermc.paper.event.inventory.ItemCraftedEvent
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack

internal class CraftingTableRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareItemCraftEvent.onPrepareCraft() {
        val player = view.player as? Player ?: return
        ShownResults.record(view, null)
        val vanillaPick = (recipe as? Keyed)?.key
        if (vanillaPick != null && recipes.isForeign(vanillaPick)) return

        val custom = recipes.findGridRecipe(inventory.matrix, vanillaPick) { it.isUsableBy(player) }
        if (custom != null) {
            if (custom.key != vanillaPick || custom.rebuildsResult) inventory.result = custom.resultFor(inventory.matrix)
            return ShownResults.record(view, inventory.result)
        }
        if (vanillaPick != null && recipes.blocksVanillaCraft(vanillaPick, inventory.matrix)) inventory.result = null
    }

    // CraftItemEvent shares this handler list, so this sees clicks on results of both vanilla and fallback recipes
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun InventoryClickEvent.onClickResult() {
        if (slotType != InventoryType.SlotType.RESULT) return
        val inventory = view.topInventory as? CraftingInventory ?: return
        val player = whoClicked as? Player ?: return
        if (inventory.result.isNullOrEmpty()) return

        // A result shown before a reload can belong to a recipe that is not registered anymore, which vanilla would still craft
        val vanillaPick = (inventory.recipe as? Keyed)?.key
        if (vanillaPick != null && !recipes.isRegistered(vanillaPick)) return run { isCancelled = true }
        if (vanillaPick != null && recipes.isForeign(vanillaPick)) return

        val custom = recipes.findGridRecipe(inventory.matrix, vanillaPick) { it.isUsableBy(player) }
        if (custom == null) return cancelIf(ShownResults.isShown(view, inventory.result) || (vanillaPick != null && recipes.getRecipe(vanillaPick) != null))
        if (custom.key == vanillaPick && !custom.isCustomConsumption) return

        // Every click is cancelled, as vanilla takes the result on some clicks that are no take action, such as onto a bundle on the cursor
        isCancelled = true
        if (isResultTake) craft(player, inventory, custom)
    }

    private fun GridRecipe.isUsableBy(player: Player) = isHandCraftable && hasPermission(player)

    private fun InventoryClickEvent.craft(player: Player, inventory: CraftingInventory, recipe: GridRecipe) {
        val matrix = inventory.matrix
        var crafted = 0
        while (crafted == 0 || (isShiftClick && recipe.isRepeatable)) {
            val matched = recipe.match(matrix) ?: break
            // Built before the slots are emptied, a result may carry components off its ingredients
            val result = recipe.resultFor(matrix)
            if (!hasRoomFor(player, result)) break

            val used = matrix.toList()
            matched.forEach { (slot, use) ->
                val consumed = use.ingredient.consume(matrix[slot] ?: return@forEach, player, use.amount)
                matrix[slot] = consumed.slot
                consumed.leftover?.let(player::giveOrDrop)
            }
            deliver(player, player.recordCraft(recipe.key, result, used))
            crafted++
        }
        if (crafted == 0) return

        // Written back once, as each assignment re-fires the prepare event for every slot
        inventory.matrix = matrix
        player.updateInventory()
    }

    // What vanilla's ResultSlot.onTake records besides consuming the grid
    private fun Player.recordCraft(key: NamespacedKey, result: ItemStack, used: List<ItemStack?>): ItemStack {
        ItemCraftedEvent(this, result).callEvent()
        val handle = (this as CraftPlayer).handle
        val crafted = CraftItemStack.asNMSCopy(result).apply { onCraftedBy(handle, count) }
        RecipeRegistry.holder(key)?.let { holder -> handle.triggerRecipeCrafted(holder, used.map { CraftItemStack.asNMSCopy(it) }) }
        discoverRecipe(key)
        return CraftItemStack.asBukkitCopy(crafted)
    }
}
