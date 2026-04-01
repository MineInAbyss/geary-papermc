package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.features.feature
import com.mineinabyss.geary.addons.world
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.createTrackOnSetBlockComponentListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import org.bukkit.block.data.BlockData
import org.kodein.di.bindSingleton
import org.kodein.di.bindSingletonOf
import org.kodein.di.instance

data class BlockTrackingModule(
    val block2Prefab: Block2Prefab,
    val prefabs: CachedQuery<GearyBlockPrefabQuery>,
) {
    fun createBlockData(prefabKey: PrefabKey): BlockData? = block2Prefab[prefabKey]
}

val BlockTracking = feature<BlockTrackingModule>("blocks") {
    dependsOn {
        condition("Block tracking disabled in config") { instance<GearyPaperConfig>().trackBlocks }
    }

    dependencies {
        bindSingletonOf(::Block2Prefab)
        bindSingleton {
            BlockTrackingModule(
                block2Prefab = instance(),
                instance<Geary>().cache(::GearyBlockPrefabQuery)
            )
        }
    }

    onEnable {
        world {
            createTrackOnSetBlockComponentListener()
        }
    }
}
