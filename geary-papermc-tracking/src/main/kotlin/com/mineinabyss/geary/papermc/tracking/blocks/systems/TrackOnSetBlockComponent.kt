package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class TrackOnSetBlockComponent : GearyListener() {
    private val TargetScope.block by get<SetBlock>()
    private val TargetScope.prefab by onSet<PrefabKey>()

    @Handler
    fun TargetScope.loadBlock() {
        val blockData = gearyBlocks.block2Prefab.blockMap.getOrDefault(block.blockType, null)?.get(block.blockId) ?: return
        gearyBlocks.block2Prefab[blockData] = prefab
    }
}
