package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.block.data.BlockData

class GearyBlockProvider {

    fun serializePrefabToBlockData(prefabKey: PrefabKey) = gearyBlocks.block2Prefab[prefabKey]

    fun deserializeBlockDataToPrefab(blockData: BlockData) = gearyBlocks.block2Prefab[blockData]
}
