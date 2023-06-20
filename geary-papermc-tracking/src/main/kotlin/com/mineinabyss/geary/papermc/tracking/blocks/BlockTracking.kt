package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.TrackOnSetBlockComponent
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.di.DI
import org.bukkit.block.data.BlockData

val gearyBlocks by DI.observe<BlockTracking>()

interface BlockTracking {
    val block2Prefab: Block2Prefab
    val blockPrefabs: GearyBlockPrefabQuery

    fun createBlockData(prefabKey: PrefabKey): BlockData? = block2Prefab[prefabKey]

    companion object : GearyAddonWithDefault<BlockTracking> {
        override fun default(): BlockTracking = object : BlockTracking {
            override val blockPrefabs = GearyBlockPrefabQuery()
            override val block2Prefab = Block2Prefab()
        }

        override fun BlockTracking.install() {
            geary.pipeline.addSystems(
                TrackOnSetBlockComponent()
            )
        }
    }
}
