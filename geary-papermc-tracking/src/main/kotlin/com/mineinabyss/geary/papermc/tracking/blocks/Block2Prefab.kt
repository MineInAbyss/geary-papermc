package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.block.data.BlockData

class Block2Prefab {
    private val prefabMap = mutableMapOf<BlockData, PrefabKey>()

    operator fun get(blockData: BlockData) = prefabMap[blockData]

    operator fun get(prefabKey: PrefabKey) = prefabMap.entries.firstOrNull { it.value == prefabKey }?.key

    operator fun set(blockData: BlockData, prefabKey: PrefabKey) {
        prefabMap[blockData] = prefabKey
    }

    operator fun contains(blockData: BlockData): Boolean = prefabMap.containsKey(blockData)

    operator fun contains(prefabKey: PrefabKey): Boolean = prefabMap.containsValue(prefabKey)
}
