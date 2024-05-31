package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun createTrackOnSetBlockComponentListener() = geary.observe<OnSet>()
    .involving(query<SetBlock, PrefabKey>())
    .exec { (block, prefab) ->
        val blockData = gearyBlocks
            .block2Prefab
            .blockMap
            .getOrDefault(block.blockType, null)
            ?.get(block.blockId) ?: return@exec

        gearyBlocks.block2Prefab[blockData] = prefab
    }
