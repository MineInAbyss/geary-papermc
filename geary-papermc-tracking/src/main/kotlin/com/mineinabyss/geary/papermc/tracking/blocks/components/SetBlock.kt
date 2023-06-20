package com.mineinabyss.geary.papermc.tracking.blocks.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:set.block")
class SetBlock (
    val blockType: BlockType,
    val blockId: Int,
) {
    enum class BlockType {
        NOTEBLOCK, WIRE, CAVEVINE, SLAB, STAIR
    }
}
