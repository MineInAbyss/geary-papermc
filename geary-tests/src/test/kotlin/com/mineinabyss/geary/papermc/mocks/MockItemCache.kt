package com.mineinabyss.geary.papermc.mocks

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class MockItemCache: PlayerItemCache<MockItem>(64) {
    override fun readItemInfo(item: MockItem): ItemInfo {
        return item.info
    }

    override fun convertToItemStack(item: MockItem): ItemStack {
        return ItemStack(Material.STONE)
    }

    override fun deserializeItem(item: MockItem): GearyEntity? {
        return entity()
    }

    override fun skipUpdate(slot: Int, newItem: MockItem?): Boolean {
        return getCachedItem(slot) === newItem
    }

    override fun skipReserialization(slot: Int, newItem: MockItem?): Boolean {
        return false
    }
}
