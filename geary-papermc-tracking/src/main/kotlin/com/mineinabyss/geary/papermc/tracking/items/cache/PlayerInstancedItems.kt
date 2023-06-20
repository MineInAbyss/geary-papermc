package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.BitSet
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.prefabs.PrefabKey

/**
 *
 */
class PlayerInstancedItems {
    private val prefab2InstanceMap = mutableMapOf<PrefabKey, GearyEntity>()
    private val prefab2SlotsMap = mutableMapOf<PrefabKey, BitSet>()

    fun add(prefabKey: PrefabKey, slot: Int, createEntity: () -> GearyEntity): GearyEntity {
        val entity = prefab2InstanceMap.getOrPut(prefabKey) { createEntity() }
        prefab2SlotsMap.getOrPut(prefabKey) { BitSet() }.set(slot)
        return entity
    }

    fun remove(prefabKey: PrefabKey, slot: Int) {
        val bits = prefab2SlotsMap[prefabKey]
        bits?.clear(slot)
        if (bits?.isEmpty() == true) {
            prefab2InstanceMap[prefabKey]?.removeEntity()
            prefab2SlotsMap.remove(prefabKey)
        }
    }

    fun hasInstance(instance: GearyEntity): Boolean = prefab2InstanceMap.containsValue(instance)

    fun removeAnyKind(slot: Int) {
        prefab2SlotsMap.toMap().forEach { (key, bits) ->
            bits.clear(slot)
            if (bits.isEmpty()) {
                prefab2InstanceMap[key]?.removeEntity()
                prefab2InstanceMap.remove(key)
                prefab2SlotsMap.remove(key)
            }
        }
    }
}
