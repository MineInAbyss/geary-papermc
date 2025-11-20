package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.entityOfOrNull
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData


private val Geary.gearyBlocks get() = getAddon(BlockTracking)

val Block.prefabKey get() = withGeary { gearyBlocks.block2Prefab[blockData] }

fun Block.toGearyOrNull() = withGeary { entityOfOrNull(gearyBlocks.block2Prefab[blockData]) }

context(world: Geary)
val BlockData.prefabKey
    get() = world.gearyBlocks.block2Prefab[this]

context(world: Geary)
fun BlockData.toGearyOrNull() = world.entityOfOrNull(world.gearyBlocks.block2Prefab[this])
