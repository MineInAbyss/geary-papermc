package com.mineinabyss.geary.papermc.features.items.recipes

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.WeakHashMap

private val TAKE_ACTIONS = setOf(
    InventoryAction.PICKUP_ALL,
    InventoryAction.PICKUP_HALF,
    InventoryAction.PICKUP_ONE,
    InventoryAction.PICKUP_SOME,
    InventoryAction.MOVE_TO_OTHER_INVENTORY,
    InventoryAction.HOTBAR_SWAP,
    InventoryAction.DROP_ALL_SLOT,
    InventoryAction.DROP_ONE_SLOT,
)

/**
 * Results put in a result slot in place of vanilla's, which vanilla would still hand out once their recipe is gone or changed.
 * Kept outside the listeners so the record outlives a reload
 */
internal object ShownResults {
    private val shown = WeakHashMap<InventoryView, ItemStack>()

    fun record(view: InventoryView, result: ItemStack?) {
        if (result.isNullOrEmpty()) shown.remove(view) else shown[view] = result.clone()
    }

    fun isShown(view: InventoryView, result: ItemStack?) = result != null && shown[view]?.isSimilar(result) == true
}

internal val InventoryClickEvent.isResultTake: Boolean get() = action in TAKE_ACTIONS

internal val InventoryClickEvent.isShiftClick: Boolean get() = action == InventoryAction.MOVE_TO_OTHER_INVENTORY

// Like vanilla, a shift-clicked result is only crafted when it fits in the inventory
internal fun InventoryClickEvent.hasRoomFor(player: Player, result: ItemStack): Boolean {
    if (!isShiftClick) return true
    val room = player.inventory.storageContents.sumOf { slot ->
        when {
            slot.isNullOrEmpty() -> result.maxStackSize
            slot.isSimilar(result) -> result.maxStackSize - slot.amount
            else -> 0
        }
    }
    return room >= result.amount
}

internal fun InventoryClickEvent.deliver(player: Player, result: ItemStack) {
    val inventory = player.inventory
    val cursor = view.cursor
    when {
        action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ONE_SLOT -> player.throwItem(result)
        click == ClickType.SWAP_OFFHAND && inventory.itemInOffHand.isEmpty -> inventory.setItemInOffHand(result)
        click == ClickType.NUMBER_KEY && inventory.getItem(hotbarButton).isNullOrEmpty() -> inventory.setItem(hotbarButton, result)
        isShiftClick || click == ClickType.SWAP_OFFHAND || click == ClickType.NUMBER_KEY -> player.giveOrDrop(result)
        cursor.isEmpty -> view.setCursor(result)
        cursor.isSimilar(result) && cursor.amount + result.amount <= cursor.maxStackSize ->
            view.setCursor(cursor.asQuantity(cursor.amount + result.amount))

        else -> player.giveOrDrop(result)
    }
}

private fun Player.throwItem(item: ItemStack) {
    val thrown = dropItem(item)
    // Paper's dropItem skips the event a drop from the inventory fires, which protection plugins rely on
    if (thrown != null && PlayerDropItemEvent(this, thrown).callEvent()) return
    thrown?.remove()
    giveOrDrop(item)
}
