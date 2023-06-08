package com.mineinabyss.geary.papermc.tracking.items.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import org.bukkit.inventory.Inventory

interface InventoryCacheWrapper {
    val cache: PlayerItemCache<*>
    fun updateToMatch(inventory: Inventory)

    fun getOrUpdate(inventory: Inventory, slot: Int): GearyEntity?
}
