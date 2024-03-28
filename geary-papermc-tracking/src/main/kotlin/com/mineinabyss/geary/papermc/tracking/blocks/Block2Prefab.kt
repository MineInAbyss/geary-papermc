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
import kotlin.math.min

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

    fun entries(): Set<Map.Entry<BlockData, PrefabKey>> = prefabMap.entries

    fun values(): Collection<PrefabKey> = prefabMap.values
    fun values(blockType: SetBlock.BlockType): Collection<PrefabKey> =
        blockMap[blockType]?.mapNotNull { prefabMap[it] } ?: emptyList()

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
            }.toTypedArray().let { putIfAbsent(SetBlock.BlockType.WIRE, it) }

            // Calculates noteblock states
            arrayListOf<BlockData>().apply {
                // Make the default blockstate the 0'th entry in the array
                add(0, Material.NOTE_BLOCK.createBlockData() as NoteBlock)

                // Max size is set to total -1 to reserve 1 state (0) for the default vanilla noteblock
                val maxSize = Instrument.entries.size * 50 - 1
                for (j in 1..<maxSize) {
                    val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                    noteBlockData.instrument = Instrument.getByType(min(Instrument.entries.size, j / 50).toByte()) ?: continue

                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j % 50 >= 25
                    add(j, noteBlockData)
                }
            }.toTypedArray().let { putIfAbsent(SetBlock.BlockType.NOTEBLOCK, it) }

            // Calculates cave-vine states
            arrayListOf<BlockData>().apply {
                add(0, Material.CAVE_VINES.createBlockData() as CaveVines)
                for (m in 0..49) {
                    val vineData = Material.CAVE_VINES.createBlockData() as CaveVines
                    vineData.isBerries = m > 25
                    vineData.age = if (m > 25) m - 25 else m
                    add(m, vineData)
                }
            }.toTypedArray().let { putIfAbsent(SetBlock.BlockType.CAVEVINE, it) }

            //Calculates slab states & stair states
            /*for (n in 1..4) {
            putIfAbsent(BLOCKY_SLABS.elementAt(n - 1).createBlockData() as Slab, n)
            putIfAbsent(BLOCKY_STAIRS.elementAt(n - 1).createBlockData() as Stairs, n)
            }*/
        }
    }
}
