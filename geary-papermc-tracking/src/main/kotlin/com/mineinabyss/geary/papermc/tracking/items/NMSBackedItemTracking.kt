package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.cache.NMSItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.NMSInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.idofront.nms.aliases.NMSItemStack

class NMSBackedItemTracking(
    val world: Geary,
) : ItemTrackingModule, Geary by world {
    override val itemProvider = GearyItemProvider(world)
    override val loginListener = LoginListener(world) { NMSItemCache(world, itemProvider) }
    override val prefabs = cache(::GearyItemPrefabQuery)

    private val itemCacheComponent = componentId<PlayerItemCache<*>>()

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get(itemCacheComponent) as? PlayerItemCache<NMSItemStack> ?: return null
        return NMSInventoryCacheWrapper(cache, entity)
    }
}
