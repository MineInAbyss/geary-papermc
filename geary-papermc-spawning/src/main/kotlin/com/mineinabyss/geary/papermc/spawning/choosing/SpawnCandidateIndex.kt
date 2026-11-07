package com.mineinabyss.geary.papermc.spawning.choosing

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.idofront.time.inWholeTicks
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.util.SimpleBitStorage
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.chunk.PalettedContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockMultiPlaceEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.block.FluidLevelChangeEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.block.SpongeAbsorbEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.lang.reflect.Field
import java.util.IdentityHashMap
import kotlin.math.floor

/**
 * Per chunk section bitsets of the blocks SpawnPositionReader could classify as GROUND or WATER.
 * Bits are decided per block type over all its states, so a set bit means the block still has to be read and a clear bit rules the position out.
 * Block type changes no listened event reports stay stale until the section is rebuilt after [SpawnConfig.spawnIndexRebuildAfter].
 * Main thread only
 */
class SpawnCandidateIndex(config: SpawnConfig) : Listener {
    enum class Lookup { UNLOADED, NOT_CANDIDATE, CANDIDATE }

    // Keeps the chunk's final section array instead of the chunk, so a lookup only reads the chunk's memory when it builds a section
    private class ChunkEntry(chunk: LevelChunk) {
        val levelSections: Array<LevelChunkSection> = chunk.sections
        val sections = arrayOfNulls<LongArray>(levelSections.size)
        val builtAt = IntArray(levelSections.size)

        // Only filled when change stamps are read
        val sources = arrayOfNulls<SectionSource>(levelSections.size)
    }

    // The containers a section's bits were read from, with their data objects and Cartridge change stamps at that time
    private class SectionSource(
        val states: PalettedContainer<BlockState>, val data: Any, val stamp: Int,
        val below: PalettedContainer<BlockState>?, val belowData: Any?, val belowStamp: Int,
    )

    private class WorldIndex(val minSectionY: Int) {
        val chunks = Long2ObjectOpenHashMap<ChunkEntry>()
    }

    private val rebuildTicks = config.spawnIndexRebuildAfter.inWholeTicks.coerceAtLeast(1).toInt()
    private val worlds = IdentityHashMap<World, WorldIndex>()
    private val blockFlags = Reference2ByteOpenHashMap<Block>().apply { defaultReturnValue(-1) }

    // Cartridge bumps this on every write to a container and on palette resizes, absent on other servers
    private val modCountField: Field? = if (!config.spawnIndexChangeStamps) null else runCatching {
        PalettedContainer::class.java.getField("cartridge\$modCount").takeIf { it.type == Int::class.javaPrimitiveType }
    }.getOrNull()

    val usesChangeStamps: Boolean get() = modCountField != null

    // Scratch space for buildSection, main thread only
    private val feetFlags = ByteArray(SECTION_BLOCKS)
    private val belowFlags = ByteArray(LAYER_BLOCKS)
    private var paletteFlags = ByteArray(16)

    fun lookup(location: Location): Lookup {
        val world = location.world as CraftWorld
        val level = world.handle
        val x = floor(location.x).toInt()
        val y = floor(location.y).toInt()
        val z = floor(location.z).toInt()
        val index = worlds.getOrPut(world) { WorldIndex(level.minSectionY) }
        val key = chunkKey(x shr 4, z shr 4)
        // An entry is only made for a loaded chunk and dropped on ChunkUnloadEvent, which fires before the chunk leaves the loaded chunk map, so finding one means the chunk is still loaded
        var entry = index.chunks.get(key)
        if (entry == null) {
            val loaded = level.chunkSource.getChunkAtIfLoadedImmediately(x shr 4, z shr 4) ?: return Lookup.UNLOADED
            entry = ChunkEntry(loaded)
            index.chunks.put(key, entry)
        }
        val sectionIndex = (y shr 4) - index.minSectionY
        val sections = entry.sections
        // Bukkit reads void air outside the build height, which can still count as GROUND on top of the highest block
        if (sectionIndex !in sections.indices) return Lookup.CANDIDATE

        val tick = Bukkit.getCurrentTick()
        var bits = sections[sectionIndex]
        if (bits == null || tick - entry.builtAt[sectionIndex] >= rebuildTicks) {
            val levelSections = entry.levelSections
            val section = levelSections[sectionIndex]
            val below = levelSections.getOrNull(sectionIndex - 1)
            if (bits == null || !unchanged(entry.sources[sectionIndex], section, below)) {
                val stamps = modCountField
                // Stamps are read before the blocks, so a write during the build leaves a stamp the next check rejects
                if (stamps != null) entry.sources[sectionIndex] = sourceOf(stamps, section, below)
                bits = buildSection(section, below)
                entry.sections[sectionIndex] = bits
            }
            entry.builtAt[sectionIndex] = tick
        }
        return if (isSet(bits, bitIndex(x and 15, y and 15, z and 15))) Lookup.CANDIDATE else Lookup.NOT_CANDIDATE
    }

    private fun sourceOf(stamps: Field, section: LevelChunkSection, below: LevelChunkSection?): SectionSource {
        val states = section.states
        val belowStates = below?.states
        return SectionSource(
            states, states.data, stamps.getInt(states),
            belowStates, belowStates?.data, if (belowStates == null) 0 else stamps.getInt(belowStates),
        )
    }

    private fun unchanged(source: SectionSource?, section: LevelChunkSection, below: LevelChunkSection?): Boolean {
        val stamps = modCountField ?: return false
        if (source == null) return false
        val states = section.states
        if (states !== source.states || states.data !== source.data || stamps.getInt(states) != source.stamp) return false
        val belowStates = below?.states
        if (belowStates !== source.below) return false
        return belowStates == null || belowStates.data === source.belowData && stamps.getInt(belowStates) == source.belowStamp
    }

    private fun buildSection(section: LevelChunkSection, below: LevelChunkSection?): LongArray {
        val onlyAir = section.hasOnlyAir()
        if (!onlyAir && !section.maybeHas { flagsOf(it) and FEET != 0 }) return EMPTY
        val belowAir = below == null || below.hasOnlyAir()
        if (!belowAir) readFlags(below!!.states, TOP_LAYER_START, belowFlags)
        if (onlyAir) {
            return if (belowAir) EMPTY else SectionCandidates.buildAir { x, z -> belowFlags[(z shl 4) or x].toInt() }
        }
        readFlags(section.states, 0, feetFlags)
        return SectionCandidates.build(
            feetFlags = { x, y, z -> feetFlags[(y shl 8) or (z shl 4) or x].toInt() },
            belowFlags = { x, z -> if (belowAir) 0 else belowFlags[(z shl 4) or x].toInt() },
        )
    }

    /** Flags of the blocks at container indexes from until from + out.size, decoded from the packed palette ids instead of reading each block */
    private fun readFlags(states: PalettedContainer<BlockState>, from: Int, out: ByteArray) {
        // One read, so palette and storage belong together even if the palette resizes meanwhile
        val data = states.data
        val palette = data.palette()
        val storage = data.storage()
        val size = palette.size
        if (size > MAX_DECODED_PALETTE || storage.bits != 0 && storage !is SimpleBitStorage) {
            for (i in out.indices) out[i] = flagsOf(states.get(from + i)).toByte()
            return
        }
        if (paletteFlags.size < size) paletteFlags = ByteArray(size)
        val flags = paletteFlags
        for (id in 0 until size) flags[id] = flagsOf(palette.valueFor(id)).toByte()
        if (storage.bits == 0) {
            out.fill(flags[0])
            return
        }
        SectionCandidates.forEachPacked(storage.raw, storage.bits, from, out.size) { i, id ->
            out[i] = if (id < size) flags[id] else UNKNOWN_FLAGS
        }
    }

    private fun flagsOf(state: BlockState): Int {
        val block = state.block
        val cached = blockFlags.getByte(block)
        if (cached >= 0) return cached.toInt()
        val flags = computeFlags(block)
        blockFlags.put(block, flags.toByte())
        return flags
    }

    // Covers every state of the block, so opening a gate or waterlogging a slab cannot clear a bit that should be set
    private fun computeFlags(block: Block): Int {
        val default = block.defaultBlockState()
        val material = default.bukkitMaterial
        val states = block.stateDefinition.possibleStates
        var flags = 0
        if (material == Material.WATER || default.hasProperty(BlockStateProperties.WATERLOGGED)) flags = flags or SectionCandidates.WATER
        val maybePassable = block.hasDynamicShape() || states.any { state ->
            runCatching { state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty }.getOrDefault(true)
        }
        if (material != Material.LAVA && material != Material.BUBBLE_COLUMN && maybePassable) flags = flags or SectionCandidates.GROUND_FEET
        if (default.`is`(BlockTags.BLOCKS_MOTION) || material == Material.SNOW) flags = flags or SectionCandidates.FLOOR
        return flags
    }

    private fun invalidate(world: World, x: Int, y: Int, z: Int) {
        val index = worlds[world] ?: return
        val entry = index.chunks.get(chunkKey(x shr 4, z shr 4)) ?: return
        val sectionIndex = (y shr 4) - index.minSectionY
        if (sectionIndex in entry.sections.indices) entry.sections[sectionIndex] = null
        // The block is the floor of the one above it
        if (y and 15 == 15 && sectionIndex + 1 in entry.sections.indices) entry.sections[sectionIndex + 1] = null
    }

    private fun invalidate(block: org.bukkit.block.Block) = invalidate(block.world, block.x, block.y, block.z)

    private fun invalidateAll(blocks: Collection<org.bukkit.block.Block>) = blocks.forEach(::invalidate)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        invalidate(block)
        if (this is BlockMultiPlaceEvent) replacedBlockStates.forEach { invalidate(it.block) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockDestroyEvent.onDestroy() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockBurnEvent.onBurn() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockFadeEvent.onFade() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockFormEvent.onForm() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockSpreadEvent.onSpread() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockGrowEvent.onGrow() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun LeavesDecayEvent.onDecay() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockFromToEvent.onFlow() = invalidate(toBlock)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun FluidLevelChangeEvent.onFluidLevel() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun SpongeAbsorbEvent.onAbsorb() = blocks.forEach { invalidate(it.block) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockExplodeEvent.onBlockExplode() = invalidateAll(blockList())

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityExplodeEvent.onEntityExplode() = invalidateAll(blockList())

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityChangeBlockEvent.onEntityChangeBlock() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun StructureGrowEvent.onStructureGrow() = blocks.forEach { invalidate(it.block) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerBucketEmptyEvent.onBucketEmpty() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerBucketFillEvent.onBucketFill() = invalidate(block)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockPistonExtendEvent.onPistonExtend() = invalidateMoved(block, blocks, direction)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun BlockPistonRetractEvent.onPistonRetract() = invalidateMoved(block, blocks, direction)

    private fun invalidateMoved(piston: org.bukkit.block.Block, blocks: List<org.bukkit.block.Block>, direction: BlockFace) {
        invalidate(piston.getRelative(direction))
        invalidate(piston.getRelative(direction.oppositeFace))
        for (block in blocks) {
            invalidate(block)
            invalidate(block.getRelative(direction))
            invalidate(block.getRelative(direction.oppositeFace))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun ChunkUnloadEvent.onUnload() {
        worlds[world]?.chunks?.remove(chunkKey(chunk.x, chunk.z))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun WorldUnloadEvent.onWorldUnload() {
        worlds.remove(world)
    }

    private companion object {
        val EMPTY = LongArray(0)
        const val FEET = SectionCandidates.WATER or SectionCandidates.GROUND_FEET
        const val SECTION_BLOCKS = 4096
        const val LAYER_BLOCKS = 256
        const val TOP_LAYER_START = SECTION_BLOCKS - LAYER_BLOCKS

        // Larger palettes are the global one, which spans every block state
        const val MAX_DECODED_PALETTE = 256

        // Stands for any block, so an id outside the palette can only add a position to read
        const val UNKNOWN_FLAGS: Byte = (SectionCandidates.WATER or SectionCandidates.GROUND_FEET or SectionCandidates.FLOOR).toByte()

        fun chunkKey(cx: Int, cz: Int): Long = (cx.toLong() shl 32) or (cz.toLong() and 0xffffffffL)

        fun bitIndex(x: Int, y: Int, z: Int) = (y shl 8) or (z shl 4) or x

        fun isSet(bits: LongArray, index: Int) = bits.isNotEmpty() && bits[index ushr 6] and (1L shl index) != 0L
    }
}

/** Candidate bits of one 16 cubed section from per block flags, kept free of server types so it can be tested */
object SectionCandidates {
    const val WATER = 1
    const val GROUND_FEET = 2
    const val FLOOR = 4

    /**
     * [feetFlags] takes section local x, y, z and [belowFlags] reads the top layer of the section below, 0 when there is none.
     * Returns 64 longs indexed by y shl 8 or z shl 4 or x, or an empty array when nothing is set
     */
    inline fun build(feetFlags: (x: Int, y: Int, z: Int) -> Int, belowFlags: (x: Int, z: Int) -> Int): LongArray {
        val bits = LongArray(64)
        val floorBelow = BooleanArray(256)
        for (z in 0..15) for (x in 0..15) floorBelow[(z shl 4) or x] = belowFlags(x, z) and FLOOR != 0
        var any = false
        for (y in 0..15) for (z in 0..15) for (x in 0..15) {
            val flags = feetFlags(x, y, z)
            val column = (z shl 4) or x
            if (flags and WATER != 0 || flags and GROUND_FEET != 0 && floorBelow[column]) {
                val index = (y shl 8) or column
                bits[index ushr 6] = bits[index ushr 6] or (1L shl index)
                any = true
            }
            floorBelow[column] = flags and FLOOR != 0
        }
        return if (any) bits else LongArray(0)
    }

    /** Same as [build] for a section of only air, where only the bottom layer can sit on a floor */
    inline fun buildAir(belowFlags: (x: Int, z: Int) -> Int): LongArray {
        var bits: LongArray? = null
        for (z in 0..15) for (x in 0..15) {
            if (belowFlags(x, z) and FLOOR == 0) continue
            val index = (z shl 4) or x
            if (bits == null) bits = LongArray(64)
            bits[index ushr 6] = bits[index ushr 6] or (1L shl index)
        }
        return bits ?: LongArray(0)
    }

    /**
     * Calls [action] with i and the value at index from + i for i below count, read from longs packed like SimpleBitStorage.
     * Values never span two longs, each long holds 64 / bits of them from its lowest bits up
     */
    inline fun forEachPacked(raw: LongArray, bits: Int, from: Int, count: Int, action: (i: Int, value: Int) -> Unit) {
        val perLong = 64 / bits
        val mask = (1L shl bits) - 1
        var longIndex = from / perLong
        var slot = from - longIndex * perLong
        var word = raw[longIndex] ushr (slot * bits)
        for (i in 0 until count) {
            if (slot == perLong) {
                longIndex++
                slot = 0
                word = raw[longIndex]
            }
            action(i, (word and mask).toInt())
            word = word ushr bits
            slot++
        }
    }
}
