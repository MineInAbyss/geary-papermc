package com.mineinabyss.geary.papermc.tracking.items.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.cache.NMSItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveInventory
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.world.entity.EquipmentSlot
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory

class NMSInventoryCacheWrapper(
    override val cache: PlayerItemCache<NMSItemStack>,
) : InventoryCacheWrapper {
    override fun updateToMatch(inventory: Inventory, ignoreCached: Boolean) = with(cache.holder.world) {
        require(inventory is PlayerInventory) { "Inventory must be a player inventory" }
        require(cache is NMSItemCache) { "Cache must be an NMS cache" }
        updateToMatch(cache, inventory, ignoreCached)
    }

    override fun getOrUpdate(inventory: Inventory, slot: Int): GearyEntity? = with(cache) {
        require(inventory is PlayerInventory) { "Geary only supports player inventories currently" }
        require(slot in 0 until PlayerItemCache.MAX_SIZE) { "Slot $slot out of bounds, must be in range 0..${PlayerItemCache.MAX_SIZE}" }
        val passive = holder.get<PassiveInventory>()
        val readAll = { toArray(inventory.toNMS(), passive) }
        return when (slot) {
            PlayerItemCache.CURSOR_SLOT -> cache.getOrUpdate(slot, inventory.holder?.itemOnCursor?.toNMS(), readAll)
            in PlayerItemCache.PASSIVE_SLOTS -> cache.getOrUpdate(slot, passive?.nmsItem(slot - PlayerItemCache.PASSIVE_SLOT_START), readAll)
            else -> cache.getOrUpdate(slot, inventory.toNMS().getItem(slot), readAll)
        }
    }

    companion object {
        fun updateToMatch(
            cache: PlayerItemCache<NMSItemStack>,
            inventory: PlayerInventory,
            ignoreCached: Boolean,
        ) {
            val passive = with(cache) { holder.get<PassiveInventory>() }
            cache.updateToMatch(toArray(inventory.toNMS(), passive), ignoreCached, inventory.heldItemSlot)
        }

        fun toArray(inventory: NMSPlayerInventory, passive: PassiveInventory?): Array<NMSItemStack?> {
            val array = Array<NMSItemStack?>(PlayerItemCache.MAX_SIZE) { null }
            // Same slots and order as Inventory.getContents, without building its combined list
            val items = inventory.nonEquipmentItems
            for (slot in 0 until items.size) array[slot] = items[slot]
            val equipment = inventory.equipment
            for (i in EQUIPMENT_SLOTS.indices) array[items.size + i] = equipment.get(EQUIPMENT_SLOTS[i])
            if (passive != null) {
                val passiveItems = (passive.inventory as CraftInventory).inventory.contents
                for (i in 0 until PassiveSlots.COUNT) array[PlayerItemCache.PASSIVE_SLOT_START + i] = passiveItems[i]
            }
            // Include cursor as last slot
            array[PlayerItemCache.CURSOR_SLOT] = inventory.player.containerMenu.carried
            return array
        }

        private val EQUIPMENT_SLOTS: Array<EquipmentSlot> = NMSPlayerInventory.EQUIPMENT_SLOT_MAPPING.int2ObjectEntrySet()
            .sortedBy { it.intKey }.map { it.value }.toTypedArray()

        private fun PassiveInventory.nmsItem(slot: Int): NMSItemStack =
            (inventory as CraftInventory).inventory.getItem(slot)
    }
}
