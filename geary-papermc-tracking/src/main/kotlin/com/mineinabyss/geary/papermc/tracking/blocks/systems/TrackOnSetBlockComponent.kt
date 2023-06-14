package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.block.data.type.*

// Adds
class TrackOnSetBlockComponent : GearyListener() {
    private val TargetScope.block by get<SetBlock>()
    private val TargetScope.prefab by onSet<PrefabKey>()

    @Handler
    fun TargetScope.loadBlock() {
        val blockData = gearyBlocks.blockMap.entries.firstOrNull {
            it.value == block.blockId &&
                    when (block.blockType) {
                        SetBlock.BlockType.NOTEBLOCK -> it.key is NoteBlock
                        SetBlock.BlockType.WIRE -> it.key is Tripwire
                        SetBlock.BlockType.CAVEVINE -> it.key is CaveVines
                        SetBlock.BlockType.SLAB -> it.key is Slab
                        SetBlock.BlockType.STAIR -> it.key is Stairs
                    }
        }?.key ?: return
        gearyBlocks.block2Prefab[blockData] = prefab
    }
}
