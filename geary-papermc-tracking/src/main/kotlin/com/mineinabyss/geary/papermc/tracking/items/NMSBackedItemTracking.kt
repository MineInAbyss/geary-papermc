package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.NMSItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.NMSInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.idofront.nms.aliases.NMSItemStack

class NMSBackedItemTracking : ItemTracking {
    override val itemProvider = GearyItemProvider()
    override val migration: ItemMigration = ItemMigration()
    override val loginListener = LoginListener { NMSItemCache() }

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get<PlayerItemCache<NMSItemStack>>() ?: return null
        return NMSInventoryCacheWrapper(cache)
    }
}
