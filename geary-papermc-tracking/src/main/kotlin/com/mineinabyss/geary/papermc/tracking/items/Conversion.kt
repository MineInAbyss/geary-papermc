package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.PlayerInventory


class GearyPlayerInventory(val inventory: PlayerInventory, val holder: HumanEntity, val cache: PlayerItemCache<NMSItemStack>) {

    /**
     * Gets or loads a Geary entity associated with the item in slot [slot] of this player's inventory.
     */
    fun get(slot: Int): GearyEntity? {
        return TODO()//cache.getOrUpdate(slot, inventory.toNMS())
    }

    // We use custom cursor slot so can't just call get
    val itemOnCursor: GearyEntity?
        get() = TODO()//cache.getOrUpdate(PlayerItemCache.CURSOR_SLOT, inventory.toNMS(), holder.itemOnCursor.toNMS())

    val itemInMainHand: GearyEntity?
        get() = get(inventory.heldItemSlot)

    val itemInOffhand: GearyEntity?
        get() = get(Inventory.SLOT_OFFHAND)

    // This is literally how bukkit gets armor slots, I'm actually sobbing.

    val itemInHelmet get() = get(inventory.size - 2)

    val itemInChestplate get() = get(inventory.size - 3)

    val itemInLeggings get() = get(inventory.size - 4)

    val itemInBoots get() = get(inventory.size - 5)
}

fun PlayerInventory.toGeary(): GearyPlayerInventory? {
    val player = holder ?: return null
    val cache = TODO() //player.toGeary().get<PlayerItemCache>() ?: return null
    return GearyPlayerInventory(this, player, cache)
}
