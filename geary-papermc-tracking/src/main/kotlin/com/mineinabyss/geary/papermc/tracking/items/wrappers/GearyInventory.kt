package com.mineinabyss.geary.papermc.tracking.items.wrappers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.GearyItemCache
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemReference
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

open class GearyInventory(
    open val inventory: Inventory
) {
    private val cache = (inventory.holder as BukkitEntity).toGeary().get<GearyItemCache>() ?: error("")

    /**
     * Gets or loads a Geary entity associated with the item in slot [slot] of this player's inventory.
     */
    operator fun get(slot: Int): GearyEntity? {
        return cache.getOrUpdate(slot, inventory.getItem(slot)?.toNMS() ?: return null)
    }

    operator fun set(index: Int, entity: GearyEntity?) {
        if(entity == null) {
            cache.remove(index, removeEntity = true)
            inventory.setItem(index, null)
            return
        }
        if(entity.has<PlayerInstancedItem>()) {

        }
        val item = entity.get<ItemStack>()
            ?: error("Tried to add entity $entity  without an ItemStack component to an inventory $inventory.")
        cache.set(index, cache.getItemReference(item.toNMS()))
        inventory.setItem(index, item)
    }

    fun getItemReferenceFromEntity(entity: GearyEntity) {
        val item = entity.get<ItemStack>() ?: return
        if(entity.has<PlayerInstancedItem>()) {
            ItemReference.Exists.PlayerInstanced(entity, item)
        }
    }

}

class PlayerGearyInventory(
    override val inventory: PlayerInventory
) : GearyInventory(inventory) {
    var itemInMainHand: GearyEntity? get() = get(inventory.heldItemSlot)
        set(value) {
            set(inventory.heldItemSlot, value)
        }

    val itemInOffhand: GearyEntity? get() = get(NMSPlayerInventory.SLOT_OFFHAND)

    val helmet get() = get(inventory.size - 2)

    val chestplate get() = get(inventory.size - 3)

    val leggings get() = get(inventory.size - 4)

    val boots get() = get(inventory.size - 5)

    val itemOnCursor: GearyEntity?
        get() {
            return cache.getOrUpdate(
                GearyItemCache.CURSOR_SLOT,
                inventory.holder?.itemOnCursor?.toNMS() ?: return null
            )
        }

}
