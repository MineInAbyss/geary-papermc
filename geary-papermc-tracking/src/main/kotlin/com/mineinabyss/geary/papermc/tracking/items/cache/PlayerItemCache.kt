package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.datastore.*
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.*
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.soywiz.kds.iterators.fastForEachWithIndex
import org.bukkit.inventory.ItemStack
import java.util.*

// TODO bad pattern, passing entity into component, move into event
class PlayerItemCache<T>(
    maxSize: Int = 64,
    val readItemInfo: (T) -> ItemInfo,
    val cacheConverter: (T) -> ItemStack,
    val deserializeItem: (T) -> GearyEntity?,
) {
    /** Entity associated with an inventory slot */
    private val entities = ULongArray(maxSize)

    /** Cache of up-to-date item references for slots. Used to avoid set calls when reference doesn't change. */
    private val cachedItems = MutableList<T?>(maxSize) { null }

    //    /** Tracks slots of items that have one instance of a prefab per player. */
    private val playerInstanced = PlayerInstancedItems()

    private fun removeEntity(slot: Int) {
        if (!playerInstanced.removeAnyKind(slot)) {
            entities[slot].takeIf { it != 0uL }?.toGeary()?.removeEntity()
        }
        entities[slot] = 0uL
    }
//    fun swap(firstSlot: Int, secondSlot: Int) {
//        val firstEntity = entities[firstSlot].toGeary()
//        val secondEntity = entities[secondSlot].toGeary()
//        if (firstEntity in playerInstanced) {
//            playerInstanced.setSlot(firstEntity, secondSlot)
//            playerInstanced.unsetSlot(firstEntity, firstSlot, false)
//        }
//        if (secondEntity in playerInstanced) {
//            playerInstanced.setSlot(secondEntity, firstSlot)
//            playerInstanced.unsetSlot(secondEntity, secondSlot, false)
//        }
//        entities[firstSlot] = secondEntity.id
//        entities[secondSlot] = firstEntity.id
////        debug("Swapped ${firstEntity.get<PrefabKey>()} in $firstSlot and ${secondEntity.get<PrefabKey>()} in $secondSlot")
//    }

//    fun move(oldSlot: Int, newSlot: Int) {
//        val entity = entities[oldSlot].takeIf { it != 0uL }?.toGeary() ?: return
//        if (entity in playerInstanced) {
//            playerInstanced.setSlot(entity, newSlot)
//            playerInstanced.unsetSlot(entity, oldSlot, false)
//        }
//        entities[newSlot] = entity.id
//        entities[oldSlot] = 0uL
////        debug("Moved ${entity.get<PrefabKey>()} from $oldSlot to $newSlot")
//    }

    /**
     * @return Whether the entity would be removed if [removeEntity] were true.
     */
//    fun remove(slot: Int, removeEntity: Boolean): Boolean {
//        val entity = entities[slot].toGeary()
//        if (entity.id == 0uL) return false
//        entities[slot] = 0uL
//        cachedItems[slot] = null
//        return if (entity.has<PlayerInstancedItem>())
//            playerInstanced.unsetSlot(entity, slot, removeEntity)
//        else if (removeEntity) {
//            entity.removeEntity()
//            true
//        } else false
//    }

    /** Sets the entity in [slot] to a loaded entity reference. */
//    fun set(slot: Int, reference: Exists) {
//        val currEntity = entities[slot].toGeary()
//        val cachedItem = cachedItems[slot]
//
//        // Update entity id in cache
//        if (reference.entity != currEntity) {
//            entities[slot] = reference.entity.id
//            if (reference is Exists.PlayerInstanced) {
//                playerInstanced.setSlot(reference.entity, slot)
//            }
//        }
//
//        // Update cached item for this entity
//        if (cachedItem !== reference.item) {
//            cachedItems[slot] = reference.item
//
//            // TODO if PlayerInstanced, use different logic so we can track ALL ItemStack references
//            if (reference.entity != NO_ENTITY) {
//                reference.entity.set<ItemStack>(CraftItemStack.asCraftMirror(reference.item))
//            }
//        }
//    }

    operator fun get(slot: Int): GearyEntity? = entities[slot].takeIf { it != 0uL }?.toGeary()

    fun getCachedItem(slot: Int): T? {
        return cachedItems[slot]
    }

//    fun updateToMatch(inventory: NMSPlayerInventory) {
//        updateToMatch(inventory.toArray())
//    }

    /** Updates cache to match passed [inventory] */
    fun updateToMatch(inventory: Array<T?>) {
//        val diffRemoved = mutableMapOf<ULong, BitSet>()
//        val diffAdded = Array<ItemInfo?>(inventory.size) { null }

//        val prefabsAdded = mutableSetOf<PrefabKey>()

        inventory.fastForEachWithIndex { slot, item ->
            if (item === cachedItems[slot]) return@fastForEachWithIndex
            cachedItems[slot] = item

            if (item == null) {
                removeEntity(slot)
            } else when (val itemInfo = readItemInfo(item)) {
                is EntityEncoded -> {
                    removeEntity(slot)
                    entities[slot] = deserializeItem(item)?.id ?: 0uL
                }

                is PlayerInstanced -> {
                    removeEntity(slot)
                    itemInfo.prefabs.forEach { prefabKey ->
                        val entity = playerInstanced.add(prefabKey, slot) {
                            entity {
                                addPrefab(prefabKey.toEntity())
                                set<ItemStack>(cacheConverter(item))
                            }
                        }
                        entities[slot] = entity.id
                    }
                }

                else -> {}
            }
        }

//        diffAdded.fastForEachWithIndex { slot, item ->
//            if (item == null) return@forEach
//
//            when (val itemInfo = readItemInfo(item)) {
//                is EntityEncoded -> {
//                    val entityId = itemInfo.entity.id
//                    if(diffRemoved.containsKey(entityId)) {
//                        diffRemoved.remove(entityId)
//                        entities[slot] = entityId
//                    }
//                }
//
//                is PlayerInstanced -> {
//                    itemInfo.prefabs.forEach { prefabKey ->
//                        playerInstanced.add(prefabKey, slot) { entity() }
//                    }
//                }
//
//                ErrorDecoding -> {}
//
//                NothingEncoded -> {}
//            }
//        }
//
//        diffRemoved.forEach { entity ->
//            entity.toGeary().removeEntity()
//        }
    }

    /**
     * Gets the entity in [slot] or updates it based in data in [item] if it doesn't match the cached item.
     *
     * @param item The item currently in the slot in the inventory.
     */
//    fun getOrUpdate(
//        slot: Int,
//        inventory: NMSPlayerInventory,
//        item: NMSItemStack? = inventory.getItem(slot)
//    ): GearyEntity? {
//        if (item !== cachedItems[slot]) {
//            updateToMatch(inventory.toArray())
//        }
//        return get(slot)
//    }

    /**
     * Gets the item reference encoded in this [item]
     */


    private fun NMSPlayerInventory.toArray(): Array<NMSItemStack?> {
        val array = Array<NMSItemStack?>(MAX_SIZE) { null }
        var slot = 0
        compartments.forEach { comp ->
            comp.forEach { item ->
                array[slot] = item
                slot++
            }
        }
        // Include cursor as last slot
        array[CURSOR_SLOT] = player.containerMenu.carried
        return array
    }

    companion object {
        const val MAX_SIZE = 64
        const val CURSOR_SLOT = 63
    }
}
