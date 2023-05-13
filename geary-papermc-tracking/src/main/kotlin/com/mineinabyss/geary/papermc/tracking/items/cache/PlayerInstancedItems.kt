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

    /**
     * @return Whether any entities were removed from the ECS
     */
    fun removeAnyKind(slot: Int): Boolean {
        var removed = false
        prefab2SlotsMap.forEach { (key, bits) ->
            bits.clear(slot)
            if (bits.isEmpty()) {
                removed = true
                prefab2InstanceMap[key]?.removeEntity()
                prefab2SlotsMap.remove(key)
            }
        }
        return removed
    }
}
