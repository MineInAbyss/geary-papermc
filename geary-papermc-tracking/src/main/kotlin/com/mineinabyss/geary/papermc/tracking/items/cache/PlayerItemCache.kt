package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.helpers.NO_ENTITY
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.datastore.*
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.*
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.geary.papermc.tracking.items.itemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.nbt.fastPDC
import com.soywiz.kds.iterators.fastForEachWithIndex
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.longs.LongArrayList
import net.minecraft.util.SortedArraySet
import net.minecraft.world.item.Items
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.*

// TODO bad pattern, passing entity into component, move into event
class PlayerItemCache(val parent: GearyEntity) {
    /** Entity associated with an inventory slot */
    private val entities = ULongArray(64)

    /** Cache of up-to-date item references for slots. Used to avoid set calls when reference doesn't change. */
    private val cachedItems = Array<NMSItemStack?>(64) { null }

    /** Tracks slots of items that have one instance of a prefab per player. */
    private val playerInstanced = PlayerInstancedItems(parent)

    fun swap(firstSlot: Int, secondSlot: Int) {
        val firstEntity = entities[firstSlot].toGeary()
        val secondEntity = entities[secondSlot].toGeary()
        if (firstEntity in playerInstanced) {
            playerInstanced.setSlot(firstEntity, secondSlot)
            playerInstanced.unsetSlot(firstEntity, firstSlot, false)
        }
        if (secondEntity in playerInstanced) {
            playerInstanced.setSlot(secondEntity, firstSlot)
            playerInstanced.unsetSlot(secondEntity, secondSlot, false)
        }
        entities[firstSlot] = secondEntity.id
        entities[secondSlot] = firstEntity.id
//        debug("Swapped ${firstEntity.get<PrefabKey>()} in $firstSlot and ${secondEntity.get<PrefabKey>()} in $secondSlot")
    }

    fun move(oldSlot: Int, newSlot: Int) {
        val entity = entities[oldSlot].takeIf { it != 0uL }?.toGeary() ?: return
        if (entity in playerInstanced) {
            playerInstanced.setSlot(entity, newSlot)
            playerInstanced.unsetSlot(entity, oldSlot, false)
        }
        entities[newSlot] = entity.id
        entities[oldSlot] = 0uL
//        debug("Moved ${entity.get<PrefabKey>()} from $oldSlot to $newSlot")
    }

    /**
     * @return Whether the entity would be removed if [removeEntity] were true.
     */
    fun remove(slot: Int, removeEntity: Boolean): Boolean {
        val entity = entities[slot].toGeary()
        if (entity.id == 0uL) return false
        entities[slot] = 0uL
        cachedItems[slot] = null
        return if (entity.has<PlayerInstancedItem>())
            playerInstanced.unsetSlot(entity, slot, removeEntity)
        else if (removeEntity) {
            entity.removeEntity()
            true
        } else false
    }

    /** Sets the entity in [slot] to a loaded entity reference. */
    fun set(slot: Int, reference: Exists) {
        val currEntity = entities[slot].toGeary()
        val cachedItem = cachedItems[slot]

        // Update entity id in cache
        if (reference.entity != currEntity) {
            entities[slot] = reference.entity.id
            if (reference is Exists.PlayerInstanced) {
                playerInstanced.setSlot(reference.entity, slot)
            }
        }

        // Update cached item for this entity
        if (cachedItem !== reference.item) {
            cachedItems[slot] = reference.item

            // TODO if PlayerInstanced, use different logic so we can track ALL ItemStack references
            if (reference.entity != NO_ENTITY) {
                reference.entity.set<ItemStack>(CraftItemStack.asCraftMirror(reference.item))
            }
        }
    }

    operator fun get(slot: Int): GearyEntity = entities[slot].toGeary()

    fun getCachedItem(slot: Int): NMSItemStack? {
        return cachedItems[slot]
    }


    /** Updates cache to match passed [inventory] */
    fun updateToMatch(inventory: Array<NMSItemStack?>) {
        val diffRemoved = mutableSetOf<ULong>()
        val diffAdded = mutableSetOf<ULong>()

        val prefabsAdded = mutableSetOf<PrefabKey>()

        inventory.fastForEachWithIndex { slot, item ->
            if (item === cachedItems[slot]) return@fastForEachWithIndex
            cachedItems[slot] = item

            val cachedEntity = entities[slot]
            diffRemoved.add(cachedEntity)

            if (item != null) {
                val itemInfo = readItemInfo(item)
                when (itemInfo) {
                    is EntityEncoded -> {
                        entities[slot] = itemInfo.entity.id
                        diffAdded.add(itemInfo.entity.id)
                    }

                    is PlayerInstanced -> {

                        playerInstanced.setSlot()
                    }
                    ErrorDecoding -> {}

                    NothingEncoded -> {}
                }
                // check if
            }

        }

        val diffMoved = diffAdded.intersect(diffRemoved)

        diffMoved.forEach { entity ->
            diffRemoved.remove(entities[slot])
            entities[slot] = encodedEntity
        }
    }

    /**
     * Gets the entity in [slot] or updates it based in data in [item] if it doesn't match the cached item.
     *
     * @param item The item currently in the slot in the inventory.
     */
    fun getOrUpdate(
        slot: Int,
        item: NMSItemStack,
    ): GearyEntity? {
        if (item === cachedItems[slot]) return get(slot)

        return when (val state = readItemInfo(item)) {
            is None -> null
            is Exists.PlayerInstanced -> {
                set(slot, state)
                state.entity
            }

            is Exists.Entity -> {
                // Entity does not match cache but already exists => either moved or duplicated
                // We cannot easily check moved here, so we assume a duplicate and let the tracker remove the old one next tick
                createAndSet(slot, NotLoaded.Entity(state.pdc, state.item))
            }

            is NotLoaded -> return createAndSet(slot, state)
        }
    }

    /** Creates an entity from an unloaded [reference] and sets it in [slot]. */
//    fun createAndSet(slot: Int, reference: NotLoaded): GearyEntity {
//        val created = itemTracking.provider.newItemEntityOrPrefab(parent, reference)
//        set(slot, created)
//        return created.entity
//    }

    fun getEntity(item: NMSItemStack): GearyEntity? {
        TODO()
    }

    /**
     * Gets the item reference encoded in this [item]
     */
    fun readItemInfo(item: NMSItemStack): ItemInfo {
        val pdc = item.fastPDC ?: return NothingEncoded
        if (item.item == Items.AIR) return NothingEncoded

        //TODO move out of here
        val didMigration = !itemTracking.migration.encodePrefabsFromCustomModelDataIfPresent(pdc, item)

        if (!pdc.hasComponentsEncoded && !didMigration) return NothingEncoded

        val prefabs = pdc.decodePrefabs().map { it.toEntityOrNull() ?: return ErrorDecoding }.toSet()

//        if (prefabs.any { it.has<PlayerInstancedItem>() }) {
//            pdc.remove<UUID>() // in case of migration
//            val existing = playerInstanced[prefabs]
//            return if (existing != null) {
//                Exists.PlayerInstanced(existing, item)
//            } else NotLoaded.PlayerInstanced(prefabs, item)
//        }

        val uuid = pdc.decode<UUID>()

        return EntityEncoded(uuid, prefabs)

        // If item doesn't have a UUID encoded or the UUID doesn't match a loaded entity, return NotLoaded
//        val entity = pdc.decode<UUID>()?.let { uuid2Geary[it] } ?: return EntityEncoded(uuid)
//        return Exists.Entity(entity, pdc, item)
    }

    companion object {
        const val MAX_SIZE = 64
        const val CURSOR_SLOT = 63
    }
}
