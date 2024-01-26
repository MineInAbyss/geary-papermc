package com.mineinabyss.geary.papermc.tracking.items.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import net.minecraft.world.entity.player.Inventory
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.spigotmc.AsyncCatcher


class GearyPlayerInventory(
    val inventory: PlayerInventory,
    val converter: InventoryCacheWrapper
) {
    /**
     * Gets or loads a Geary entity associated with the item in slot [slot] of this player's inventory.
     */
    fun get(slot: Int): GearyEntity? {
        return converter.getOrUpdate(inventory, slot)
    }

    fun forceRefresh(ignoreCached: Boolean = false) {
        converter.updateToMatch(inventory, ignoreCached)
    }

    /**
     * Gets or loads a Geary entity associated with the item in equipmentSlot [equipmentSlot] of this player's inventory.
     */
    fun get(equipmentSlot: EquipmentSlot): GearyEntity? {
        return when (equipmentSlot) {
            EquipmentSlot.HAND -> itemInMainHand
            EquipmentSlot.OFF_HAND -> itemInOffhand
            EquipmentSlot.HEAD -> itemInHelmet
            EquipmentSlot.CHEST -> itemInChestplate
            EquipmentSlot.LEGS -> itemInLeggings
            EquipmentSlot.FEET -> itemInBoots
        }
    }

    fun find(itemStack: ItemStack): GearyEntity? {
        return get(inventory.indexOf(itemStack))
    }

    // We use custom cursor slot so can't just call get
    val itemOnCursor: GearyEntity? get() = get(PlayerItemCache.CURSOR_SLOT)

    val itemInMainHand: GearyEntity?
        get() = get(inventory.heldItemSlot)

    val itemInOffhand: GearyEntity?
        get() = get(Inventory.SLOT_OFFHAND)

    // This is literally how bukkit gets armor slots, I'm actually sobbing.

    val itemInHelmet get() = get(inventory.size - 2)

    val itemInChestplate get() = get(inventory.size - 3)

    val itemInLeggings get() = get(inventory.size - 4)

    val itemInBoots get() = get(inventory.size - 5)
}

fun PlayerInventory.toGeary(): GearyPlayerInventory? {
    try {
        if (gearyPaper.config.catchAsyncEntityConversion)
            AsyncCatcher.catchOp("Async geary inventory access for $holder")
    } catch (_: NoClassDefFoundError) {
        // Allow running in tests
    }
    val player = holder ?: return null
    val wrap = gearyItems.getCacheWrapper(player.toGearyOrNull() ?: return null) ?: return null
    return GearyPlayerInventory(this, wrap)
}
