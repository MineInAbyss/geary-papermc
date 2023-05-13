package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.world.entity.player.Inventory
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.PlayerInventory


class GearyPlayerInventory(
    val inventory: PlayerInventory,
    val holder: HumanEntity,
    val cache: PlayerItemCache<NMSItemStack>
) {
    val nmsInv = inventory.toNMS()
    /**
     * Gets or loads a Geary entity associated with the item in slot [slot] of this player's inventory.
     */
    fun get(slot: Int): GearyEntity? {
        return cache.getOrUpdate(slot, nmsInv.getItem(slot)) { toArray(inventory) }
    }

    // We use custom cursor slot so can't just call get
    val itemOnCursor: GearyEntity?
        get() = cache.getOrUpdate(PlayerItemCache.CURSOR_SLOT, holder.itemOnCursor.toNMS()) { toArray(inventory) }

    val itemInMainHand: GearyEntity?
        get() = get(inventory.heldItemSlot)

    val itemInOffhand: GearyEntity?
        get() = get(Inventory.SLOT_OFFHAND)

    // This is literally how bukkit gets armor slots, I'm actually sobbing.

    val itemInHelmet get() = get(inventory.size - 2)

    val itemInChestplate get() = get(inventory.size - 3)

    val itemInLeggings get() = get(inventory.size - 4)

    val itemInBoots get() = get(inventory.size - 5)

    companion object {
        fun toArray(inventory: PlayerInventory): Array<NMSItemStack?> {
            val array = Array<NMSItemStack?>(PlayerItemCache.MAX_SIZE) { null }
            var slot = 0
            val nmsInv = inventory.toNMS()
            nmsInv.compartments.forEach { comp ->
                comp.forEach { item ->
                    array[slot] = item
                    slot++
                }
            }
            // Include cursor as last slot
            array[PlayerItemCache.CURSOR_SLOT] = nmsInv.player.containerMenu.carried
            return array
        }
    }
}

fun PlayerInventory.toGeary(): GearyPlayerInventory? {
    val player = holder ?: return null
    val cache = player.toGeary().get<PlayerItemCache<NMSItemStack>>() ?: return null
    return GearyPlayerInventory(this, player, cache)
}
