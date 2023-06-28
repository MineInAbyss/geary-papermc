package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.nbt.fastPDC
import org.bukkit.inventory.ItemStack

class NMSItemCache : PlayerItemCache<NMSItemStack>(64) {
    override fun readItemInfo(item: NMSItemStack): ItemInfo {
        return LoginListener.readItemInfo(item)
    }

    override fun convertToItemStack(item: NMSItemStack): ItemStack {
        return item.toBukkit()
    }

    override fun deserializeItem(item: NMSItemStack): GearyEntity? {
        return gearyItems.itemProvider.deserializeItemStackToEntity(item.fastPDC)
    }

    /**
     * The underlying item pointer does not get updated when throwing an item out of inventory,
     * it's just emptied, so we do an extra check to see if an item has been emptied, but isn't empty in the cache
     */
    override fun skipUpdate(slot: Int, newItem: NMSItemStack?): Boolean {
        return getCachedItem(slot) === newItem && !(get(slot) != null && newItem?.isEmpty == true)
    }
}
