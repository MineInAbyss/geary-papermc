package com.mineinabyss.geary.papermc.tracking.blocks

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.GearyBlockPrefabQuery
import com.mineinabyss.geary.papermc.tracking.blocks.systems.TrackOnSetBlockComponent
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.di.DI
import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.*

val gearyBlocks by DI.observe<BlockTracking>()
interface BlockTracking {
    val blockMap: Map<BlockData, Int>
    val blockProvider: GearyBlockProvider
    val block2Prefab: Block2Prefab
    val blockPrefabs: GearyBlockPrefabQuery

    fun createBlockData(prefabKey: PrefabKey): BlockData? = blockProvider.serializePrefabToBlockData(prefabKey)

    companion object : GearyAddonWithDefault<BlockTracking> {
        override fun default(): BlockTracking = object : BlockTracking {
            override val blockMap = createBlockMap()
            override val blockProvider = GearyBlockProvider()
            override val blockPrefabs = GearyBlockPrefabQuery()
            override val block2Prefab = Block2Prefab()
        }

        override fun BlockTracking.install() {
            geary.pipeline.addSystems(
                TrackOnSetBlockComponent()
            )
        }

        private fun createBlockMap(): Map<BlockData, Int> {
            return mutableMapOf<BlockData, Int>().apply {
                // Calculates tripwire states
                for (i in 0..127) {
                    val tripWireData = Material.TRIPWIRE.createBlockData() as Tripwire
                    if (i and 1 == 1) tripWireData.setFace(BlockFace.NORTH, true)
                    if (i shr 1 and 1 == 1) tripWireData.setFace(BlockFace.EAST, true)
                    if (i shr 2 and 1 == 1) tripWireData.setFace(BlockFace.SOUTH, true)
                    if (i shr 3 and 1 == 1) tripWireData.setFace(BlockFace.WEST, true)
                    if (i shr 4 and 1 == 1) tripWireData.isPowered = true
                    if (i shr 5 and 1 == 1) tripWireData.isDisarmed = true
                    if (i shr 6 and 1 == 1) tripWireData.isAttached = true

                    putIfAbsent(tripWireData, i)
                }

                // Calculates noteblock states
                // We do 25-825 to skip PIANO at first
                for (j in 50..799) {
                    //val id = if (blocky.config.noteBlocks.restoreNormalFunctionality && j <= 50) j + 799 else j
                    val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                    noteBlockData.instrument = Instrument.getByType((j / 50 % 400).toByte()) ?: continue

                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1

                    putIfAbsent(noteBlockData, j - 49)
                }
                // Restore functionality bs
                for (j in 1..49) {
                    val noteBlockData = Material.NOTE_BLOCK.createBlockData() as NoteBlock
                    noteBlockData.instrument = Instrument.PIANO
                    noteBlockData.note = Note((j % 25))
                    noteBlockData.isPowered = j / 25 % 2 == 1

                    putIfAbsent(noteBlockData, j + 750)
                }

                // Calculates cave-vine states
                for (m in 1..50) {
                    val vineData = Material.CAVE_VINES.createBlockData() as CaveVines
                    vineData.isBerries = m > 25
                    vineData.age = if (m > 25) m - 25 else m
                    putIfAbsent(vineData, m)
                }

                //Calculates slab states & stair states
                /*for (n in 1..4) {
                    putIfAbsent(BLOCKY_SLABS.elementAt(n - 1).createBlockData() as Slab, n)
                    putIfAbsent(BLOCKY_STAIRS.elementAt(n - 1).createBlockData() as Stairs, n)
                }*/
            }
        }
    }
}
