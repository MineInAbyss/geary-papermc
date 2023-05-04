package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab

/**
 *
 */
class PlayerInstancedItems {
    val logger get() = geary.logger

    /** Map of prefab entity to its instance on the player */
    private val prefab2InstanceMap = mutableMapOf<PrefabKey, GearyEntity>()
    private val prefab2SlotsMap = mutableMapOf<PrefabKey, BitSet>()

    fun add(prefabKey: PrefabKey, slot: Int) {
        prefab2InstanceMap[prefabKey]

        prefab2SlotsMap.getOrPut(prefabKey) { BitSet() }.set(slot)
    }

    fun remove(prefabKey: PrefabKey, slot: Int) {
        val bits = prefab2SlotsMap[prefabKey]
        bits?.clear(slot)
        if(bits?.isEmpty() == true) {
            prefab2InstanceMap[prefabKey]?.removeEntity()
            prefab2SlotsMap.remove(prefabKey)
        }
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
}
