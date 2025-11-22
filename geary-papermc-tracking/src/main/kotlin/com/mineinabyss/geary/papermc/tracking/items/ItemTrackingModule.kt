package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import org.bukkit.inventory.ItemStack

interface ItemTrackingModule {
    val itemProvider: GearyItemProvider
    val prefabs: CachedQuery<GearyItemPrefabQuery>
    fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper?

    fun createCache(holder: GearyEntity): PlayerItemCache<*>

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null,
    ): ItemStack? = itemProvider.serializePrefabToItemStack(prefabKey, writeTo)
}