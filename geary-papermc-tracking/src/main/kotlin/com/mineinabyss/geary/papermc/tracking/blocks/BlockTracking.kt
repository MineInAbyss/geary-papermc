package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.createTrackOnSetBlockComponentListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import org.bukkit.block.data.BlockData

data class BlockTrackingModule(
    val block2Prefab: Block2Prefab,
    val prefabs: CachedQuery<GearyBlockPrefabQuery>,
) {
    fun createBlockData(prefabKey: PrefabKey): BlockData? = block2Prefab[prefabKey]
}

val BlockTracking = createAddon<Block2Prefab, BlockTrackingModule>("Block Tracking", { Block2Prefab() }) {
    val module = BlockTrackingModule(configuration, prefabs = geary.cache(::GearyBlockPrefabQuery))
    entities {
        createTrackOnSetBlockComponentListener(module.block2Prefab)
    }
    module
}
