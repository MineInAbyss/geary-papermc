package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import org.bukkit.inventory.ItemStack

class BukkitItemCache(
    world: Geary,
    private val gearyItems: ItemTrackingModule
) : PlayerItemCache<ItemStack>(world, 64) {
    override fun readItemInfo(item: ItemStack): ItemInfo {
        return LoginListener.readItemInfo(item)
    }

    override fun convertToItemStack(item: ItemStack): ItemStack {
        return item
    }

    override fun deserializeItem(item: ItemStack): GearyEntity? {
        var entity: GearyEntity? = null
        item.editPersistentDataContainer {
            entity = gearyItems.itemProvider.deserializeItemStackToEntity(it)
        }
        return entity
    }

    override fun skipUpdate(slot: Int, newItem: ItemStack?): Boolean {
        return getCachedItem(slot) === newItem && !(get(slot) != null && newItem?.amount == 0)
    }

    override fun skipReserialization(slot: Int, newItem: ItemStack?): Boolean {
        return getCachedItem(slot)?.equals(newItem) == true
    }
}
