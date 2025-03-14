package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.fastForEachWithIndex
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.EntityEncoded
import com.mineinabyss.geary.papermc.tracking.items.components.Equipped
import com.mineinabyss.geary.papermc.tracking.items.components.InHand
import com.mineinabyss.geary.papermc.tracking.items.components.InInventory
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.inventory.ItemStack

abstract class PlayerItemCache<T>(
    world: Geary,
    val maxSize: Int = 64,
) : Geary by world {
    /** Entity associated with an inventory slot */
    private val entities = ULongArray(maxSize)

    /** Cache of up-to-date item references for slots. Used to avoid set calls when reference doesn't change. */
    private val cachedItems = MutableList<T?>(maxSize) { null }

    var previousHeldSlot = -1

    private fun removeEntity(slot: Int) {
        val entity = entities[slot].takeIf { it != 0uL }?.toGeary() ?: return
        logger.v { "Removing ${entities[slot]} in slot $slot" }
        val pdc = entity.get<ItemStack>()?.itemMeta?.persistentDataContainer
        if (pdc != null) entity.encodeComponentsTo(pdc)
        entity.removeEntity()
        entities[slot] = 0uL
    }

    fun getEntities(): List<GearyEntity?> {
        return entities.map { it.takeIf { it != 0uL }?.toGeary() }.toList()
    }

    operator fun get(slot: Int): GearyEntity? = entities[slot].takeIf { it != 0uL }?.toGeary()

    fun getCachedItem(slot: Int): T? {
        return cachedItems[slot]
    }

    /** Updates cache to match passed [inventory] */
    fun updateToMatch(
        inventory: Array<T?>,
        holder: GearyEntity? = null,
        ignoreCached: Boolean = false,
        heldSlot: Int = -1,
    ) {
        inventory.fastForEachWithIndex { slot, item ->
            // Generally an identical item reference => the same item, but not vice versa
            // To avoid expensive equality checks, we do a quick check to skip updates followed by an equality check for re-serialization.
            // (ex. an item reference is updated when a player scrolls through their inventory, but it's still the same item, so we don't skipUpdate, but do skipReserialization)
            if (!ignoreCached && skipUpdate(slot, item)) return@fastForEachWithIndex
            if (!ignoreCached && skipReserialization(slot, item)) {
                cachedItems[slot] = item
                if (item != null) get(slot)?.set<ItemStack>(convertToItemStack(item))
                return@fastForEachWithIndex
            }
            cachedItems[slot] = item

            if (item == null) {
                removeEntity(slot)
            } else when (readItemInfo(item)) {
                is EntityEncoded -> {
                    removeEntity(slot)
                    val newEntity = deserializeItem(item)
                    entities[slot] = newEntity?.id ?: 0uL
                    if (newEntity == null) {
                        logger.v { "Decoded null entity in $slot" }
                        return@fastForEachWithIndex
                    }
                    if (holder != null) newEntity.addParent(holder)
                    newEntity.set<ItemStack>(convertToItemStack(item))
                    newEntity.add<InInventory>()

                    // Add components based on slot
                    when (slot) {
                        in 37..40 -> newEntity.add<Equipped>()
                    }

                    logger.v { "Adding $newEntity (${newEntity.prefabs.map { it.get<PrefabKey>() }}) in slot $slot" }
                }

                else -> removeEntity(slot)
            }
        }
        if (heldSlot != previousHeldSlot) {
            if (previousHeldSlot != -1) entities[previousHeldSlot].toGeary().remove<InHand>()
            if (heldSlot != -1) entities[heldSlot].toGeary().add<InHand>()
            previousHeldSlot = heldSlot
        }
    }

    fun clear() {
        repeat(maxSize) { removeEntity(it) }
    }

    /**
     * Gets the entity in [slot] or updates it based in data in [item] if it doesn't match the cached item.
     *
     * @param item The item currently in the slot in the inventory.
     */
    fun getOrUpdate(
        slot: Int,
        item: T?,
        readInventoryContents: () -> Array<T?>,
    ): GearyEntity? {
        if (item !== cachedItems[slot]) {
            updateToMatch(readInventoryContents())
        }
        return get(slot)
    }

    abstract fun readItemInfo(item: T): ItemInfo
    abstract fun convertToItemStack(item: T): ItemStack
    abstract fun deserializeItem(item: T): GearyEntity?
    abstract fun skipUpdate(slot: Int, newItem: T?): Boolean
    abstract fun skipReserialization(slot: Int, newItem: T?): Boolean

    companion object {
        const val MAX_SIZE = 64
        const val CURSOR_SLOT = 63
    }
}
