package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.blocks.Block2Prefab
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun Geary.createTrackOnSetBlockComponentListener(blocks: Block2Prefab) = observe<OnSet>()
    .involving(query<SetBlock, PrefabKey>())
    .exec { (block, prefab) ->
        val blockData = blocks
            .blockMap
            .getOrDefault(block.blockType, null)
            ?.get(block.blockId) ?: return@exec

        blocks[blockData] = prefab
    }
