package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Tripwire

// Adds
class TrackOnSetBlockComponent : GearyListener() {
    private val TargetScope.block by onSet<SetBlock>()

    @Handler
    fun TargetScope.loadBlock() {
        gearyBlocks.block2Prefab[gearyBlocks.blockMap.entries.firstOrNull {
            it.value == block.blockId &&
            when (block.blockType) {
                SetBlock.BlockType.NOTEBLOCK -> it.key is NoteBlock
                SetBlock.BlockType.WIRE -> it.key is Tripwire
                SetBlock.BlockType.CAVEVINE -> it.key is CaveVines
                SetBlock.BlockType.SLAB -> it.key is Slab
                SetBlock.BlockType.STAIR -> it.key is Stairs
            }
        }?.key ?: return] = entity.prefabs.firstOrNull()?.get<PrefabKey>() ?: return
    }
}
