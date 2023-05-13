@file:Suppress("UNREACHABLE_CODE")

package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.datatypes.forEachBit
import com.mineinabyss.geary.datatypes.pop1
import com.mineinabyss.geary.datatypes.toIntArray
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.*
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.time.ticks
import com.soywiz.kds.iterators.fastForEachWithIndex
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
class InventoryTrackerSystem : RepeatingSystem(interval = 1.ticks) {
    private val TargetScope.player by get<Player>()
    private val TargetScope.itemCache by get<PlayerItemCache<NMSItemStack>>()

    override fun TargetScope.tick() {
        TODO()
//        itemCache.updateToMatch(player.toNMS().inventory)
    }
}
