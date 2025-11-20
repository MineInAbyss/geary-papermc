package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.createTrackOnSetBlockComponentListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.idofront.features.feature
import org.bukkit.block.data.BlockData
import org.koin.core.module.dsl.scopedOf

data class BlockTrackingModule(
    val block2Prefab: Block2Prefab,
    val prefabs: CachedQuery<GearyBlockPrefabQuery>,
) {
    fun createBlockData(prefabKey: PrefabKey): BlockData? = block2Prefab[prefabKey]
}

val BlockTracking = feature<BlockTrackingModule>("blocks") {
    dependsOn {
        condition("Block tracking disabled in config") { get<GearyPaperConfig>().trackBlocks }
    }

    scopedModule {
        scopedOf(::Block2Prefab)
        scoped {
            BlockTrackingModule(
                block2Prefab = get(),
                get<Geary>().cache(::GearyBlockPrefabQuery)
            )
        }
    }
    configureGeary {
        onEnable {
            addCloseables(
                createTrackOnSetBlockComponentListener(get<Block2Prefab>())
            )
        }
    }
}
