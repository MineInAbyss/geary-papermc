package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.setBit
import com.mineinabyss.geary.datatypes.toIntArray
import com.mineinabyss.geary.datatypes.unsetBit
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap

/**
 *
 */
class PlayerInstancedItems(val parent: GearyEntity) {
    val logger get() = geary.logger

    /** Map of prefab entity to its instance on the player */
    private val prefab2InstanceMap: Long2LongOpenHashMap = Long2LongOpenHashMap()
    private val instance2PrefabMap: Long2LongOpenHashMap = Long2LongOpenHashMap()

    private val slots: Long2LongOpenHashMap = Long2LongOpenHashMap()

    fun setSlot(entity: GearyEntity, slot: Int) {
        val (prefab, instance) = entity.pair()
        // If entity not present
        if (prefab == 0L) {
            instantiatePrefab(entity, slot)
            return
        }
        val curr = slots[instance]
        slots[instance] = curr.or(1L shl slot)
    }

    fun unsetSlot(entity: GearyEntity, slot: Int, removeEntity: Boolean): Boolean {
        val (_, instance) = entity.pair()
        val update = slots[instance].unsetBit(slot)
        if (update == 0L) {
            remove(entity, removeEntity)
            return true
        }
        slots[instance] = update
        return false
    }

    fun getSlots(entity: GearyEntity): IntArray {
        val (_, instance) = entity.pair()
        val slots = slots[instance]
        return slots.toIntArray()
    }

    fun setSlots(entity: GearyEntity, newSlots: IntArray) {
        val (_, instance) = entity.pair()
        var slotLong = 0L
        for (slot in newSlots) slotLong = slotLong.setBit(slot)
        slots[instance] = slotLong
    }

    operator fun get(entity: GearyEntity): GearyEntity? {
        val id = entity.id.toLong()
        return (prefab2InstanceMap[id].takeIf { it != 0L }
            ?: instance2PrefabMap[id].takeIf { it != 0L })?.toGeary()
    }

    operator fun contains(entity: GearyEntity): Boolean {
        val id = entity.id.toLong()
        return prefab2InstanceMap[id] != 0L || slots.containsKey(id)
    }

    private fun instantiatePrefab(prefab: GearyEntity, vararg addToSlots: Int): GearyEntity {
        //TODO perhaps we want to disallow adding to no slots
        val prefabId = prefab.id.toLong()

        val instance = prefab2InstanceMap.getOrPut(prefabId) {
            entity {
                addPrefab(prefab)
                addParent(parent)
                logger.d("Loaded prefab ${prefab.get<PrefabKey>()} on $parent")
            }.id.toLong()
        }
        instance2PrefabMap[instance] = prefabId
        setSlots(prefab, addToSlots)
        return instance.toGeary()
    }

    private fun remove(entity: GearyEntity, removeEntity: Boolean): Boolean {
        val (prefab, instance) = entity.pair()
        if (prefab == 0L) return false
        prefab2InstanceMap.remove(prefab)
        instance2PrefabMap.remove(instance)
        slots.remove(instance) != 0L
        if (removeEntity) instance.toGeary().removeEntity()
        return true
    }

    private fun GearyEntity.pair(): Pair<Long, Long> {
        val id = id.toLong()
        val read = prefab2InstanceMap[id]
        return if (read != 0L) id to read else instance2PrefabMap[id] to id
    }
}
