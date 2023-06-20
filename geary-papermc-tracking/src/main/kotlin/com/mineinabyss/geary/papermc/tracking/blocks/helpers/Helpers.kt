package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

val Block.prefabKey get() = gearyBlocks.block2Prefab[blockData]
val BlockData.prefabKey get() = gearyBlocks.block2Prefab[this]
fun Block.toGearyOrNull() = gearyBlocks.block2Prefab[blockData]?.toEntityOrNull()
fun BlockData.toGearyOrNull() = gearyBlocks.block2Prefab[this]?.toEntityOrNull()
