package com.mineinabyss.geary.papermc.spawning.spread_spawn

import kotlinx.coroutines.future.await
import net.minecraft.SharedConstants
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.level.chunk.storage.SerializableChunkData
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.CraftBlockType

// Reads a chunk's saved block states without loading it, so there is no ticket, no entities and no load events
class SavedChunkReader(world: World, materialFlags: MaterialFlags) {
    private val level = (world as CraftWorld).handle
    private val chunkMap = level.chunkSource.chunkMap
    private val codec = level.palettedContainerFactory().blockStatesContainerCodec()
    private val minHeight = world.minHeight
    private val maxHeight = world.maxHeight
    private val minSectionY = level.minSectionY
    private val sectionCount = level.sectionsCount
    private val stateFlags = ByteArray(Block.BLOCK_STATE_REGISTRY.size()).also { flags ->
        for (state in Block.BLOCK_STATE_REGISTRY) {
            val waterlogged = state.getValueOrElse(BlockStateProperties.WATERLOGGED, false)
            flags[Block.BLOCK_STATE_REGISTRY.getId(state)] = materialFlags.of(CraftBlockType.minecraftToBukkit(state.block), waterlogged).toByte()
        }
    }

    /** Null when the chunk was never saved fully generated */
    suspend fun read(cx: Int, cz: Int): ChunkGrid? {
        val saved = chunkMap.read(ChunkPos(cx, cz)).await().orElse(null) ?: return null
        // Chunks saved by an older Minecraft version need the same data fixers a load runs
        val tag = if (NbtUtils.getDataVersion(saved) < CURRENT_DATA_VERSION) chunkMap.upgradeChunkTag(saved) else saved
        if (!SerializableChunkData.getChunkStatusFromTag(tag).isOrAfter(ChunkStatus.FULL)) return null

        val sections = arrayOfNulls<PalettedContainer<BlockState>>(sectionCount)
        for (section in tag.getListOrEmpty(SerializableChunkData.SECTIONS_TAG).compoundStream()) {
            val index = section.getByteOr("Y", 0.toByte()) - minSectionY
            if (index !in 0 until sectionCount) continue
            val states = section.getCompound("block_states").orElse(null) ?: continue
            sections[index] = codec.parse(NbtOps.INSTANCE, states).getOrThrow { IllegalStateException("Invalid block states in chunk $cx, $cz: $it") }
        }

        return ChunkGrid.build(minHeight, maxHeight, { i -> sections[i]?.maybeHas { !it.isAir } != true }) { x, y, z ->
            val state = sections[(y - minHeight) shr 4]!!.get(x, y and 15, z)
            stateFlags[Block.BLOCK_STATE_REGISTRY.getId(state)].toInt()
        }
    }

    private companion object {
        val CURRENT_DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version()
    }
}
