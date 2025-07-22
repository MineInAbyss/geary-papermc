package com.mineinabyss.geary.papermc.tracking.items.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.papermc.tracking.items.cache.NMSItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.mineinabyss.idofront.nms.aliases.toNMS
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory

class NMSInventoryCacheWrapper(
    override val cache: PlayerItemCache<NMSItemStack>,
    val holder: GearyEntity,
) : InventoryCacheWrapper {
    override fun updateToMatch(inventory: Inventory, ignoreCached: Boolean) = with(holder.world) {
        require(inventory is PlayerInventory) { "Inventory must be a player inventory" }
        require(cache is NMSItemCache) { "Cache must be an NMS cache" }
        updateToMatch(cache, holder, inventory, ignoreCached)
    }

    override fun getOrUpdate(inventory: Inventory, slot: Int): GearyEntity? {
        require(inventory is PlayerInventory) { "Geary only supports player inventories currently" }
        require(slot in 0 until PlayerItemCache.MAX_SIZE) { "Slot $slot out of bounds, must be in range 0..${PlayerItemCache.MAX_SIZE}" }
        return if (slot == PlayerItemCache.CURSOR_SLOT) {
            cache.getOrUpdate(
                PlayerItemCache.CURSOR_SLOT,
                inventory.holder?.itemOnCursor?.toNMS()
            ) { toArray(inventory.toNMS()) }
        } else {
            cache.getOrUpdate(slot, inventory.toNMS().getItem(slot)) { toArray(inventory.toNMS()) }
        }
    }

    companion object {
        fun updateToMatch(
            cache: PlayerItemCache<NMSItemStack>,
            holder: GearyEntity,
            inventory: PlayerInventory,
            ignoreCached: Boolean,
        ) {
            cache.updateToMatch(toArray(inventory.toNMS()), holder, ignoreCached, inventory.heldItemSlot)
        }

        fun toArray(inventory: NMSPlayerInventory): Array<NMSItemStack?> {
            val array = Array<NMSItemStack?>(PlayerItemCache.MAX_SIZE) { null }
            var slot = 0
            inventory.contents.fastForEach { item ->
                array[slot] = item
                slot++
            }
            // Include cursor as last slot
            array[PlayerItemCache.CURSOR_SLOT] = inventory.player.containerMenu.carried
            return array
        }
    }
}
