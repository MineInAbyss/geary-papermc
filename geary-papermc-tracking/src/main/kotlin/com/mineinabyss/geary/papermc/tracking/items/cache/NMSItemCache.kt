package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.itemTracking
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.toBukkit
import org.bukkit.inventory.ItemStack

class NMSItemCache: PlayerItemCache<NMSItemStack>(64) {
    override fun readItemInfo(item: NMSItemStack): ItemInfo {
        return LoginListener.readItemInfo(item)
    }

    override fun convertToItemStack(item: NMSItemStack): ItemStack {
        return item.toBukkit()
    }

    override fun deserializeItem(item: NMSItemStack): GearyEntity? {
        return itemTracking.provider.deserializeItemStackToEntity(item)
    }

    override fun skipUpdate(slot: Int, newItem: NMSItemStack?): Boolean {
        return getCachedItem(slot) === newItem && !(get(slot) != null && newItem?.isEmpty == true)
    }

}
