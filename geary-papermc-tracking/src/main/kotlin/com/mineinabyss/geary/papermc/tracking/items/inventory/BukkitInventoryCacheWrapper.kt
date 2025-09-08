package com.mineinabyss.geary.papermc.tracking.items.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class BukkitInventoryCacheWrapper(
    override val cache: PlayerItemCache<ItemStack>
) : InventoryCacheWrapper {
    override fun updateToMatch(inventory: Inventory, ignoreCached: Boolean) = with(cache) {
        updateToMatch(inventory.toArray(), ignoreCached)
    }

    override fun getOrUpdate(inventory: Inventory, slot: Int): GearyEntity? {
        return cache.getOrUpdate(slot, inventory.getItem(slot)) { inventory.toArray() }
    }

    companion object {
        fun Inventory.toArray(): Array<ItemStack?> {
            val cursor = (this as? PlayerInventory)?.holder?.itemOnCursor
            return contents.plus(cursor)
        }
    }
}
