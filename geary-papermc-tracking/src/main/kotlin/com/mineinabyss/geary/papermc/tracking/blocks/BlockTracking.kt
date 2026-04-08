package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyWorld
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

val BlockTracking = module("blocks") {
    require(get<GearyPaperConfig>().trackBlocks) { "Block tracking disabled in config" }

    val block2Prefab by single { new(::Block2Prefab) }
    single {
        BlockTrackingModule(
            block2Prefab = block2Prefab,
            prefabs = get<Geary>().cache(::GearyBlockPrefabQuery)
        )
    }

    gearyWorld {
        createTrackOnSetBlockComponentListener()
    }
}.gets<BlockTrackingModule>()
