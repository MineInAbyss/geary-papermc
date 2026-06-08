package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.entityOfOrNull
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


private val WorldScoped.gearyBlocks get() = getAddon(BlockTracking)

val Block.prefabKey get() = withGeary { gearyBlocks.block2Prefab[blockData] }

fun Block.toGearyOrNull() = withGeary { entityOfOrNull(gearyBlocks.block2Prefab[blockData]) }

context(world: WorldScoped)
val BlockData.prefabKey
    get() = world.gearyBlocks.block2Prefab[this]

context(world: WorldScoped)
fun BlockData.toGearyOrNull() = world.entityOfOrNull(world.gearyBlocks.block2Prefab[this])
