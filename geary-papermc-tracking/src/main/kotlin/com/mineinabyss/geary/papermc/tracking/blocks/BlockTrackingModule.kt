package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.addons.createGearyAddon
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.createTrackOnSetBlockComponentListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.idofront.di.DI
import org.bukkit.block.data.BlockData

val gearyBlocks by geary.di.observe<BlockTrackingModule>()

open class BlockTrackingModule {
    val prefabs = geary.cache(GearyBlockPrefabQuery())
    val block2Prefab = Block2Prefab()

    fun createBlockData(prefabKey: PrefabKey): BlockData? = block2Prefab[prefabKey]
}

class BlockTrackingConfig(
    var module: () -> BlockTrackingModule = ::BlockTrackingModule,
)

val BlockTracking = createGearyAddon(::BlockTrackingConfig) {
    application.di.add(config.module())
    application.createTrackOnSetBlockComponentListener()
}
