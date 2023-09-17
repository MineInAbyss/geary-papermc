package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class TrackOnSetBlockComponent : GearyListener() {
    private val Pointers.block by get<SetBlock>().on(target)
    private val Pointers.prefab by get<PrefabKey>().whenSetOnTarget()

    override fun Pointers.handle() {
        val blockData = gearyBlocks
            .block2Prefab
            .blockMap
            .getOrDefault(block.blockType, null)
            ?.get(block.blockId) ?: return

        gearyBlocks.block2Prefab[blockData] = prefab
    }
}
