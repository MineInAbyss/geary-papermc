package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.prefabs.entityOfOrNull
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


context(Geary)
private val gearyBlocks get() = getAddon(BlockTracking)

context(Geary)
val Block.prefabKey get() = gearyBlocks.block2Prefab[blockData]

context(Geary)
val BlockData.prefabKey get() = gearyBlocks.block2Prefab[this]

context(Geary)
fun Block.toGearyOrNull() = entityOfOrNull(gearyBlocks.block2Prefab[blockData])

context(Geary)
fun BlockData.toGearyOrNull() = entityOfOrNull(gearyBlocks.block2Prefab[this])
