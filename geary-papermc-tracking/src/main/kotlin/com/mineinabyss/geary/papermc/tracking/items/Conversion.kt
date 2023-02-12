package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.world.entity.player.Inventory
import org.bukkit.entity.HumanEntity

/**
 * Gets or loads a Geary entity associated with the item in slot [slot] of this player's inventory.
 */
fun HumanEntity.getGearyItem(slot: Int): GearyEntity? {
    return toGeary().get<PlayerItemCache>()?.getOrUpdate(slot, inventory.getItem(slot)?.toNMS() ?: return null)
}

// We use custom cursor slot so can't just call getGearyItem
val HumanEntity.gearyItemOnCursor: GearyEntity?
    get() {
        return toGeary().get<PlayerItemCache>()
            ?.getOrUpdate(PlayerItemCache.CURSOR_SLOT, itemOnCursor.toNMS() ?: return null)
    }

val HumanEntity.gearyItemInMainHand: GearyEntity?
    get() = getGearyItem(inventory.heldItemSlot)

val HumanEntity.gearyItemInOffhand: GearyEntity?
    get() = getGearyItem(Inventory.SLOT_OFFHAND)

// This is literally how bukkit gets armor slots, I'm actually sobbing.

val HumanEntity.gearyItemInHelmet get() = getGearyItem(inventory.size - 2)

val HumanEntity.gearyItemInChestplate get() = getGearyItem(inventory.size - 3)

val HumanEntity.gearyItemInLeggings get() = getGearyItem(inventory.size - 4)

val HumanEntity.gearyItemInBoots get() = getGearyItem(inventory.size - 5)
