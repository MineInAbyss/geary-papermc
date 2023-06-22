package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.BukkitItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.BukkitInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import org.bukkit.inventory.ItemStack

class BukkitBackedItemTracking : ItemTracking {
    override val itemProvider = GearyItemProvider()
    override val migration: ItemMigration = ItemMigration()
    override val loginListener = LoginListener { BukkitItemCache() }
    override val prefabs = GearyItemPrefabQuery()

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get<PlayerItemCache<ItemStack>>() ?: return null
        return BukkitInventoryCacheWrapper(cache)
    }
}
