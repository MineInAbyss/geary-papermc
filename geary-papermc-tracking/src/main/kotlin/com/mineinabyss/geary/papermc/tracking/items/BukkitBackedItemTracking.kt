package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.tracking.items.cache.BukkitItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.BukkitInventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import org.bukkit.inventory.ItemStack

class BukkitBackedItemTracking(
    val world: Geary,
) : ItemTrackingModule {
    override val itemProvider = GearyItemProvider(world)
    override val loginListener = LoginListener(world) { BukkitItemCache(it, world.getAddon(ItemTracking)) }
    override val prefabs = world.cache(::GearyItemPrefabQuery)

    override fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper? {
        val cache = entity.get<PlayerItemCache<ItemStack>>() ?: return null
        return BukkitInventoryCacheWrapper(cache)
    }
}
