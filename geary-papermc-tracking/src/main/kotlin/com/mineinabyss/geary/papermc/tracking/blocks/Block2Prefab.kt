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
import org.bukkit.block.data.type.Slab
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
                for (j in 0..Instrument.entries.size * 50) {
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

            //Calculates slab, stair, door, trapdoor & grate states
            // We add AIR first to  offset all the ids to 1-4
            listOf(Material.AIR, Material.WAXED_CUT_COPPER_SLAB, Material.WAXED_EXPOSED_CUT_COPPER_SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, Material.WAXED_WEATHERED_CUT_COPPER_SLAB)
                .map(Material::createBlockData).toTypedArray().let { putIfAbsent(SetBlock.BlockType.SLAB, it) }

            listOf(Material.AIR, Material.WAXED_CUT_COPPER_STAIRS, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
                .map(Material::createBlockData).toTypedArray().let { putIfAbsent(SetBlock.BlockType.STAIR, it) }

            listOf(Material.AIR, Material.WAXED_COPPER_DOOR, Material.WAXED_EXPOSED_COPPER_DOOR, Material.WAXED_OXIDIZED_COPPER_DOOR, Material.WAXED_WEATHERED_COPPER_DOOR)
                .map(Material::createBlockData).toTypedArray().let { putIfAbsent(SetBlock.BlockType.DOOR, it) }

            listOf(Material.AIR, Material.WAXED_COPPER_TRAPDOOR, Material.WAXED_EXPOSED_COPPER_TRAPDOOR, Material.WAXED_OXIDIZED_COPPER_TRAPDOOR, Material.WAXED_WEATHERED_COPPER_TRAPDOOR)
                .map(Material::createBlockData).toTypedArray().let { putIfAbsent(SetBlock.BlockType.TRAPDOOR, it) }

            listOf(Material.AIR, Material.WAXED_COPPER_GRATE, Material.WAXED_EXPOSED_COPPER_GRATE, Material.WAXED_OXIDIZED_COPPER_GRATE, Material.WAXED_WEATHERED_COPPER_GRATE)
                .map(Material::createBlockData).toTypedArray().let { putIfAbsent(SetBlock.BlockType.GRATE, it) }
        }
    }
}
