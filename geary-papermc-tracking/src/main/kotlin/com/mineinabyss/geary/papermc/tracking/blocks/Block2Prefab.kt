package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.prefabs
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*

class Block2Prefab {
    private val prefabMap = mutableMapOf<BlockData, PrefabKey>()

    operator fun get(blockData: BlockData) = prefabMap[blockData]

    operator fun get(prefabKey: PrefabKey) = prefabMap.entries.firstOrNull { it.value == prefabKey }?.key

    operator fun set(blockData: BlockData, prefabKey: PrefabKey) {
        prefabMap[blockData] = prefabKey
    }

    operator fun contains(blockData: BlockData): Boolean = prefabMap.containsKey(blockData)

    operator fun contains(prefabKey: PrefabKey): Boolean = prefabMap.containsValue(prefabKey)

    /*private fun createPrefabMap(): Map<BlockData, PrefabKey> {

        return mutableMapOf<BlockData, PrefabKey>().run { ->
            gearyBlocks.blockPrefabs.getKeys().forEach { prefabKey ->
                prefabKey.toEntityOrNull()?.let { entity ->
                    entity.get<SetBlock>()?.let { gearyBlock ->
                        val blockData = blockMap.entries.filter {
                            when (gearyBlock.blockType) {
                                SetBlock.BlockType.NOTEBLOCK -> it.key is NoteBlock
                                SetBlock.BlockType.WIRE -> it.key is Tripwire
                                SetBlock.BlockType.CAVEVINE -> it.key is CaveVinesPlant
                                SetBlock.BlockType.SLAB -> it.key is Slab
                                SetBlock.BlockType.STAIR -> it.key is Stairs
                                // Note: This apparently is needed otherwise "WhenExpression is not exhaustive"
                                else -> return@forEach
                            }
                        }.firstOrNull { it.value == gearyBlock.blockId }?.key ?: return@forEach

                        put(blockData, prefabKey)
                    }
                }
            }
            this
        }
    }*/
}
