package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.cache.NMSItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.NMSInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.systems.builders.cachedQuery
import com.mineinabyss.idofront.nms.aliases.NMSItemStack

class NMSBackedItemTracking : ItemTracking {
    override val itemProvider = GearyItemProvider()
    override val migration: ItemMigration = ItemMigration()
    override val loginListener = LoginListener { NMSItemCache() }
    override val prefabs = geary.cachedQuery(GearyItemPrefabQuery())

    private val itemCacheComponent = componentId<PlayerItemCache<*>>()

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get(itemCacheComponent) as? PlayerItemCache<NMSItemStack> ?: return null
        return NMSInventoryCacheWrapper(cache, entity)
    }
}
