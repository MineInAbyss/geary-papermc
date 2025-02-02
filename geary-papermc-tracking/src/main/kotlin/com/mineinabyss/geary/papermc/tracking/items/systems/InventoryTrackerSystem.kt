@file:Suppress("UNREACHABLE_CODE")

package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.inventory.NMSInventoryCacheWrapper
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player

/**
 * ItemStack instances are super disposable, they don't represent real items. Additionally, tracking items is
 * very inconsistent, so we must cache all components from an item, then periodically check to ensure these items
 * are still there, alongside all the item movement events available to us.
 *
 * ## Process:
 * - An Inventory component stores a cache of items, which we read and compare to actual items in the inventory.
 * - We go through geary items in the inventory and ensure the right items match our existing slots.
 * - If an item is a mismatch, we add it to a list of mismatches
 * - If an item isn't in our cache, we check the mismatches or deserialize it into the cache.
 * - All valid items get re-serialized TODO in the future there should be some form of dirty tag so we aren't unnecessarily serializing things
 */
fun Geary.createInventoryTrackerSystem() = system(
    query<Player, PlayerItemCache<*>>()
).every(1.ticks).exec { query ->
    val (player, itemCache) = query
    NMSInventoryCacheWrapper.updateToMatch(
        itemCache as PlayerItemCache<NMSItemStack>,
        @OptIn(UnsafeAccessors::class) // updateToMatch doesn't call any entity writes on this entity
        query.unsafeEntity.toGeary(),
        player.inventory,
        ignoreCached = false
    )
}
