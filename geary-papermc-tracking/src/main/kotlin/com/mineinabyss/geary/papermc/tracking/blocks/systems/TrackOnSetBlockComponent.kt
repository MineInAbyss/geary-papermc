package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun createTrackOnSetBlockComponentListener() = geary.listener(
    object : ListenerQuery() {
        val block by get<SetBlock>()
        val prefab by get<PrefabKey>()
    }
).exec {
    val blockData = gearyBlocks
        .block2Prefab
        .blockMap
        .getOrDefault(block.blockType, null)
        ?.get(block.blockId) ?: return@exec

    gearyBlocks.block2Prefab[blockData] = prefab
}
