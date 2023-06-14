package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

val Block.prefabKey get() = gearyBlocks.blockProvider.deserializeBlockDataToPrefab(blockData)
val BlockData.prefabKey get() = gearyBlocks.blockProvider.deserializeBlockDataToPrefab(this)
fun Block.toGearyOrNull() = gearyBlocks.blockProvider.deserializeBlockDataToPrefab(blockData)?.toEntityOrNull()
fun BlockData.toGearyOrNull() = gearyBlocks.blockProvider.deserializeBlockDataToPrefab(this)?.toEntityOrNull()
