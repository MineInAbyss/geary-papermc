package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.CaveVines
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire

class Block2Prefab {
    val blockMap = createBlockMap()
    private val prefabMap = mutableMapOf<BlockData, PrefabKey>()

    operator fun get(blockData: BlockData) = prefabMap[blockData]

    operator fun get(prefabKey: PrefabKey) = prefabMap.entries.firstOrNull { it.value == prefabKey }?.key

    operator fun set(blockData: BlockData, prefabKey: PrefabKey) {
        prefabMap[blockData] = prefabKey
    }

    operator fun contains(blockData: BlockData): Boolean = prefabMap.containsKey(blockData)

    operator fun contains(prefabKey: PrefabKey): Boolean = prefabMap.containsValue(prefabKey)

    private fun createBlockMap(): MutableMap<SetBlock.BlockType, Array<BlockData>> {
        return mutableMapOf<SetBlock.BlockType, Array<BlockData>>().apply {
            // Calculates tripwire states
            arrayListOf<BlockData>().apply {
                for (i in 0..127) {
                    val tripWireData = Material.TRIPWIRE.createBlockData() as Tripwire
                    if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
                    if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
                    if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
                    if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
                    if (i shr 4 and 1 == 1) tripWireData.isPowered = true
                    if (i shr 5 and 1 == 1) tripWireData.isDisarmed = true
                    if (i shr 6 and 1 == 1) tripWireData.isAttached = true
                    add(i, tripWireData)
                }
            }.toTypedArray().let { put(SetBlock.BlockType.WIRE, it) }

            // Calculates noteblock states
            // We do 25-825 to skip PIANO at first
            arrayListOf<BlockData>().apply {
                val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                for (j in 50..799) {
                    //val id = if (blocky.config.noteBlocks.restoreNormalFunctionality && j <= 50) j + 799 else j
                    noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue

                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1
                    add(j - 50, noteBlockData)
                }

                // Restore functionality bs
                for (j in 0..49) {
                    noteBlockData.instrument = Instrument.PIANO
                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1
                    add(j + 750, noteBlockData)
                }
            }.toTypedArray().let { put(SetBlock.BlockType.NOTEBLOCK, it) }

            // Calculates cave-vine states
            arrayListOf<BlockData>().apply {
                for (m in 0..49) {
                    val vineData = Material.CAVE_VINES.createBlockData() as CaveVines
                    vineData.isBerries = m > 25
                    vineData.age = if (m > 25) m - 25 else m
                    add(m, vineData)
                }
            }.toTypedArray().let { put(SetBlock.BlockType.CAVEVINE, it) }
        }

        //Calculates slab states & stair states
        /*for (n in 1..4) {
            putIfAbsent(BLOCKY_SLABS.elementAt(n - 1).createBlockData() as Slab, n)
            putIfAbsent(BLOCKY_STAIRS.elementAt(n - 1).createBlockData() as Stairs, n)
        }*/
    }
}
