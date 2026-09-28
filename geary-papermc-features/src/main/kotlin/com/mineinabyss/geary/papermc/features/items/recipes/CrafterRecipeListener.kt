package com.mineinabyss.geary.papermc.features.items.recipes

import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.inventory.CrafterMenu
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.level.block.CrafterBlock
import net.minecraft.world.level.block.entity.HopperBlockEntity
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.Crafter
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.inventory.view.CraftCrafterView
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.Container as NmsContainer
import net.minecraft.world.item.ItemStack as NmsItemStack
import org.bukkit.block.data.type.Crafter as CrafterData

internal class CrafterRecipeListener(private val recipes: RecipeManager) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun CrafterCraftEvent.onCrafterCraft() {
        val matrix = block.crafterInventory()?.contents ?: return
        when (val outcome = outcome(recipe.key, matrix)) {
            null -> isCancelled = true
            else -> outcome.result?.let { result = it }
        }
    }

    // Vanilla fills the result slot from its own pick, which the craft may then cancel or replace
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun InventoryOpenEvent.trackCrafterPreview() {
        val menu = (view as? CraftCrafterView)?.handle as? CrafterMenu ?: return
        val level = (player as? CraftPlayer)?.handle?.level() ?: return
        val resultSlot = menu.slots.firstOrNull { it.container is ResultContainer } ?: return
        val grid = menu.container as? CraftingContainer ?: return
        // Added after the menu's own listener, so this runs right after vanilla refreshes the result
        menu.addSlotListener(object : ContainerListener {
            override fun slotChanged(menu: AbstractContainerMenu, slot: Int, stack: NmsItemStack) {
                val pick = CrafterBlock.getPotentialResults(level, grid.asCraftInput()).getOrNull() ?: return
                val matrix = grid.contents.map { CraftItemStack.asBukkitCopy(it).takeUnless(ItemStack::isEmpty) }.toTypedArray()
                val outcome = outcome(pick.toBukkitRecipe().let { (it as Keyed).key }, matrix)
                when {
                    outcome == null -> resultSlot.set(NmsItemStack.EMPTY)
                    outcome.result != null -> resultSlot.set(CraftItemStack.asNMSCopy(outcome.result))
                }
            }

            override fun dataChanged(menu: AbstractContainerMenu, id: Int, value: Int) = Unit
        })
    }

    /** Null when the craft is refused, otherwise the result replacing vanilla's, which is null when vanilla's stands */
    private fun outcome(vanillaPick: NamespacedKey, matrix: Array<ItemStack?>): CrafterOutcome? {
        // Crafters pick from a cache that can hold recipes removed since, and a removed geary recipe would look foreign
        if (!recipes.isRegistered(vanillaPick)) return null
        if (recipes.isForeign(vanillaPick)) return CrafterOutcome(null)

        val custom = findRecipe(matrix, vanillaPick)
            ?: return if (recipes.blocksVanillaCraft(vanillaPick, matrix)) null else CrafterOutcome(null)
        if (custom.isCustomConsumption && consumeBeforeVanilla(custom, matrix) == null) return null

        return CrafterOutcome(if (custom.key != vanillaPick || custom.rebuildsResult) custom.resultFor(matrix) else null)
    }

    private class CrafterOutcome(val result: ItemStack?)

    // Vanilla leaves the slots alone when the craft is cancelled or its result cleared, so they are only touched once that is final
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun CrafterCraftEvent.consumeIngredients() {
        if (result.isEmpty || recipes.isForeign(recipe.key)) return
        val (inventory, matrix) = (block.crafterInventory() ?: return).let { it to it.contents }
        val custom = findRecipe(matrix, recipe.key)?.takeIf { it.isCustomConsumption } ?: return
        val consumed = consumeBeforeVanilla(custom, matrix) ?: return

        consumed.forEach { (slot, slotConsumption) -> inventory.setItem(slot, slotConsumption.slot) }
        block.dispense(inventory, consumed.values.flatMap { it.dispensed })
    }

    // There is no player to check a permission against, crafter recipes drop theirs at load
    private fun findRecipe(matrix: Array<ItemStack?>, vanillaPick: NamespacedKey) =
        recipes.findGridRecipe(matrix, vanillaPick) { it.permission == null }

    private fun Block.crafterInventory() = (getState(false) as? Crafter)?.inventory

    private fun consumeBeforeVanilla(recipe: GridRecipe, matrix: Array<ItemStack?>): Map<Int, SlotConsumption>? {
        val matched = recipe.match(matrix) ?: return null
        return matched.mapValues { (slot, use) -> use.consumeBeforeVanilla(matrix[slot] ?: return null) ?: return null }
    }

    /**
     * Vanilla shrinks every slot by one and dispenses one crafting remainder per slot right after this event.
     * Slots are set one above what they should end up with, and only what vanilla does not dispense is dispensed here.
     * Null when vanilla's part cannot be made up for, such as when keeping a full stack or damaging a tool
     */
    private fun RecipeMatcher.SlotUse.consumeBeforeVanilla(item: ItemStack): SlotConsumption? {
        val vanillaRemainder = item.craftingRemainder()
        return when (val consumption = ingredient.consumption) {
            is Consumption.Damage -> null
            Consumption.Keep -> SlotConsumption(item.asQuantity(item.amount + 1), emptyList())
                .takeIf { vanillaRemainder == null && item.amount < item.maxStackSize }

            is Consumption.Consume -> {
                val replacement = consumption.replacement
                if (replacement != null && vanillaRemainder != null && !replacement.isSimilar(vanillaRemainder)) return null
                val remainder = replacement ?: vanillaRemainder
                val owed = (remainder?.amount ?: 0) * amount - (vanillaRemainder?.amount ?: 0)
                SlotConsumption(item.asQuantity(item.amount - amount + 1), remainder?.inStacks(owed).orEmpty())
            }
        }
    }

    // Into the container in front like vanilla's own output
    private fun Block.dispense(source: Inventory, items: List<ItemStack>) {
        if (items.isEmpty()) return
        val front = BlockFace.valueOf((blockData as CrafterData).orientation.name.substringBefore('_'))
        val target = getRelative(front)
        val container = (target.getState(false) as? Container)?.inventory
        items.forEach { item ->
            val overflow = container?.let { insert(source, it, item) { moved -> addTo(target, front, moved, it) } } ?: listOf(item)
            overflow.forEach { world.dropItem(target.location.toCenterLocation(), it) }
        }
    }

    // Protection plugins rely on the move event vanilla fires for crafter output
    private fun insert(source: Inventory, into: Inventory, item: ItemStack, add: (ItemStack) -> Collection<ItemStack>): Collection<ItemStack> {
        val event = InventoryMoveItemEvent(source, item, into, true)
        if (!event.callEvent()) return listOf(event.item)
        return add(event.item)
    }

    // Vanilla feeds a crafter in front one item at a time through hopper insertion, which skips its disabled slots and spreads the items out
    private fun Block.addTo(target: Block, front: BlockFace, item: ItemStack, into: Inventory): Collection<ItemStack> {
        if (target.getState(false) !is Crafter) return into.addItem(item).values
        val level = (world as CraftWorld).handle
        val from = level.getBlockEntity((this as CraftBlock).position) as? NmsContainer ?: return listOf(item)
        val crafter = level.getBlockEntity((target as CraftBlock).position) as? NmsContainer ?: return listOf(item)
        val remaining = CraftItemStack.asNMSCopy(item)
        while (!remaining.isEmpty) {
            if (!HopperBlockEntity.addItem(from, crafter, remaining.copyWithCount(1), CraftBlock.blockFaceToNotch(front.oppositeFace)).isEmpty) break
            remaining.shrink(1)
        }
        return listOfNotNull(CraftItemStack.asBukkitCopy(remaining).takeUnless { it.isEmpty })
    }

    private class SlotConsumption(val slot: ItemStack, val dispensed: List<ItemStack>)
}
