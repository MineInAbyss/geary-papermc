package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.entityOfOrNull
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


private val Geary.gearyBlocks get() = getAddon(BlockTracking)

val Block.prefabKey get() = withGeary { gearyBlocks.block2Prefab[blockData] }

fun Block.toGearyOrNull() = withGeary { entityOfOrNull(gearyBlocks.block2Prefab[blockData]) }

context(Geary)
val BlockData.prefabKey
    get() = gearyBlocks.block2Prefab[this]

context(Geary)
fun BlockData.toGearyOrNull() = entityOfOrNull(gearyBlocks.block2Prefab[this])
