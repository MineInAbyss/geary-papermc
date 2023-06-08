package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.itemTracking
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import org.bukkit.inventory.ItemStack

class BukkitItemCache : PlayerItemCache<ItemStack>(64) {
    override fun readItemInfo(item: ItemStack): ItemInfo {
        return LoginListener.readItemInfo(item)
    }

    override fun convertToItemStack(item: ItemStack): ItemStack {
        return item
    }

    override fun deserializeItem(item: ItemStack): GearyEntity? {
        return itemTracking.itemProvider.deserializeItemStackToEntity(item.itemMeta.persistentDataContainer)
    }

    override fun skipUpdate(slot: Int, newItem: ItemStack?): Boolean {
        return getCachedItem(slot) === newItem && !(get(slot) != null && newItem?.amount == 0)
    }
}
