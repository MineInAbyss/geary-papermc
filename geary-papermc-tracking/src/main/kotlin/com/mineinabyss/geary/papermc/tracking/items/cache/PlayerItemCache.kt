package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.fastForEachWithIndex
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.EntityEncoded
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.inventory.ItemStack

abstract class PlayerItemCache<T>(
    val maxSize: Int = 64,
) {
    private val logger get() = geary.logger

    /** Entity associated with an inventory slot */
    private val entities = ULongArray(maxSize)

    /** Cache of up-to-date item references for slots. Used to avoid set calls when reference doesn't change. */
    private val cachedItems = MutableList<T?>(maxSize) { null }

    private fun removeEntity(slot: Int) {
        val entity = entities[slot].takeIf { it != 0uL }?.toGeary() ?: return
        logger.d("Removing ${entities[slot]} in slot $slot")
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
    fun updateToMatch(inventory: Array<T?>, holder: GearyEntity? = null, ignoreCached: Boolean = false) {
        inventory.fastForEachWithIndex { slot, item ->
            if (!ignoreCached && skipUpdate(slot, item)) return@fastForEachWithIndex
            cachedItems[slot] = item

            if (item == null) {
                removeEntity(slot)
            } else when (val itemInfo = readItemInfo(item)) {
                is EntityEncoded -> {
                    removeEntity(slot)
                    val newEntity = deserializeItem(item)
                    entities[slot] = newEntity?.id ?: 0uL
                    if (holder != null) newEntity?.addParent(holder)
                    newEntity?.set<ItemStack>(convertToItemStack(item))
                    logger.d("Adding $newEntity (${newEntity?.prefabs?.map { it.get<PrefabKey>() }}) in slot $slot")
                }

                else -> removeEntity(slot)
            }
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

    companion object {
        const val MAX_SIZE = 64
        const val CURSOR_SLOT = 63
    }
}
