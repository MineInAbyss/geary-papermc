package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.cache.BukkitItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.BukkitInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.systems.builders.cache
import org.bukkit.inventory.ItemStack

class BukkitBackedItemTracking : ItemTracking {
    override val itemProvider = GearyItemProvider()
    override val loginListener = LoginListener { BukkitItemCache() }
    override val prefabs = geary.cache(GearyItemPrefabQuery())

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get<PlayerItemCache<ItemStack>>() ?: return null
        return BukkitInventoryCacheWrapper(cache)
    }
}
