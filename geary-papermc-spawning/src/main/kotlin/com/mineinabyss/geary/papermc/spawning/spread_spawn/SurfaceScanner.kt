package com.mineinabyss.geary.papermc.spawning.spread_spawn

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random
import kotlin.time.Duration

object SurfaceFlags {
    const val FLOOR = 1
    const val PASSABLE = 2
    const val LIQUID = 4
}

/** Block flags of one chunk, indexed by local x and z (0..15) and absolute y in [minHeight, maxHeight) */
class ChunkGrid(
    val minHeight: Int,
    val maxHeight: Int,
    private val flags: ByteArray,
) {
    init {
        require(flags.size == 256 * (maxHeight - minHeight)) { "Grid size does not match world height" }
    }

    // IsOpenAreaCondition reads y = maxHeight, which Bukkit returns as void air
    fun flagsAt(x: Int, y: Int, z: Int): Int = when {
        y >= maxHeight -> SurfaceFlags.PASSABLE
        y < minHeight -> 0
        else -> flags[index(x, y, z)].toInt()
    }

    private fun index(x: Int, y: Int, z: Int) = ((y - minHeight) shl 8) or (z shl 4) or x

    companion object {
        inline fun build(
            minHeight: Int,
            maxHeight: Int,
            isSectionEmpty: (Int) -> Boolean,
            flagsAt: (x: Int, y: Int, z: Int) -> Int,
        ): ChunkGrid {
            val flags = ByteArray(256 * (maxHeight - minHeight))
            for (section in 0 until (maxHeight - minHeight + 15) / 16) {
                val sectionMinY = minHeight + section * 16
                val sectionMaxY = minOf(sectionMinY + 16, maxHeight)
                if (isSectionEmpty(section)) {
                    flags.fill(SurfaceFlags.PASSABLE.toByte(), (sectionMinY - minHeight) shl 8, (sectionMaxY - minHeight) shl 8)
                    continue
                }
                for (y in sectionMinY until sectionMaxY) for (z in 0..15) for (x in 0..15) {
                    flags[((y - minHeight) shl 8) or (z shl 4) or x] = flagsAt(x, y, z).toByte()
                }
            }
            return ChunkGrid(minHeight, maxHeight, flags)
        }
    }
}

/**
 * @property histogram Count of every standable position by openness, index 0..[SurfaceScanner.MAX_OPENNESS]
 * @property positions Reservoir sample of positions with openness of at least 1, packed with [SurfacePos]
 * @property scannedAt Epoch seconds
 */
class ChunkSurface(
    val histogram: IntArray,
    val positions: IntArray,
    val version: Int = SurfaceScanner.VERSION,
    val scannedAt: Long = 0,
) {
    fun withoutPosition(lx: Int, lz: Int, yOffset: Int): ChunkSurface {
        val location = SurfacePos.pack(lx, lz, yOffset, 0)
        val remaining = positions.filter { it and SurfacePos.LOCATION_MASK != location }
        if (remaining.size == positions.size) return this
        return ChunkSurface(histogram, remaining.toIntArray(), version, scannedAt)
    }

    fun needsScan(nowSeconds: Long, rescanAfter: Duration): Boolean =
        version != SurfaceScanner.VERSION || nowSeconds - scannedAt > rescanAfter.inWholeSeconds

    fun encodeHistogram(): ByteArray {
        val buffer = ByteBuffer.allocate(histogram.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        histogram.forEach { buffer.putShort(it.coerceIn(0, 0xffff).toShort()) }
        return buffer.array()
    }

    fun encodePositions(): ByteArray {
        val buffer = ByteBuffer.allocate(positions.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        positions.forEach { buffer.putInt(it) }
        return buffer.array()
    }

    companion object {
        fun decode(histogram: ByteArray, positions: ByteArray, version: Int, scannedAt: Long): ChunkSurface {
            val hist = ByteBuffer.wrap(histogram).order(ByteOrder.LITTLE_ENDIAN)
            val pos = ByteBuffer.wrap(positions).order(ByteOrder.LITTLE_ENDIAN)
            return ChunkSurface(
                histogram = IntArray(histogram.size / 2) { hist.getShort().toInt() and 0xffff },
                positions = IntArray(positions.size / 4) { pos.getInt() },
                version = version,
                scannedAt = scannedAt,
            )
        }
    }
}

/** Packs lx(4) | lz(4) | y - minHeight(12) | openness(3) into an Int */
object SurfacePos {
    const val MAX_HEIGHT_RANGE = 1 shl 12
    const val LOCATION_MASK = 0xfffff

    fun pack(lx: Int, lz: Int, yOffset: Int, openness: Int): Int =
        lx or (lz shl 4) or (yOffset shl 8) or (openness shl 20)

    fun lx(packed: Int) = packed and 0xf
    fun lz(packed: Int) = (packed shr 4) and 0xf
    fun yOffset(packed: Int) = (packed shr 8) and 0xfff
    fun openness(packed: Int) = (packed shr 20) and 0x7
}

fun chunkKey(cx: Int, cz: Int): Long = (cx.toLong() shl 32) or (cz.toLong() and 0xffffffffL)
fun chunkKeyX(key: Long): Int = (key shr 32).toInt()
fun chunkKeyZ(key: Long): Int = key.toInt()

object SurfaceScanner {
    const val VERSION = 2
    const val MAX_OPENNESS = 7

    fun prefixSize(grid: ChunkGrid) = 17 * 17 * (grid.maxHeight - grid.minHeight + 1)

    /** Fills [prefix] with a 3D prefix sum of blocks that are not passable, so any box can be tested in constant time */
    fun buildBlockedPrefix(grid: ChunkGrid, prefix: IntArray = IntArray(prefixSize(grid))): IntArray {
        val height = grid.maxHeight - grid.minHeight
        for (y in 0..height) for (z in 0..16) for (x in 0..16) {
            val i = prefixIndex(x, y, z)
            if (x == 0 || y == 0 || z == 0) {
                prefix[i] = 0
                continue
            }
            val blocked = if (grid.flagsAt(x - 1, grid.minHeight + y - 1, z - 1) and SurfaceFlags.PASSABLE == 0) 1 else 0
            prefix[i] = blocked +
                    prefix[prefixIndex(x - 1, y, z)] + prefix[prefixIndex(x, y - 1, z)] + prefix[prefixIndex(x, y, z - 1)] -
                    prefix[prefixIndex(x - 1, y - 1, z)] - prefix[prefixIndex(x - 1, y, z - 1)] - prefix[prefixIndex(x, y - 1, z - 1)] +
                    prefix[prefixIndex(x - 1, y - 1, z - 1)]
        }
        return prefix
    }

    fun isStandable(grid: ChunkGrid, x: Int, y: Int, z: Int): Boolean {
        if (grid.flagsAt(x, y - 1, z) and SurfaceFlags.FLOOR == 0) return false
        val feet = grid.flagsAt(x, y, z)
        return feet and SurfaceFlags.PASSABLE != 0 && feet and SurfaceFlags.LIQUID == 0
    }

    /**
     * Largest r up to [MAX_OPENNESS] where the box x±r, y..y+r, z±r is passable.
     * X and z are clamped to the chunk to match IsOpenAreaCondition, which never reads outside the location's chunk.
     * Blocks above the world count as passable
     */
    fun openness(grid: ChunkGrid, x: Int, y: Int, z: Int, prefix: IntArray = buildBlockedPrefix(grid)): Int {
        if (grid.flagsAt(x, y, z) and SurfaceFlags.PASSABLE == 0) return -1
        val localY = y - grid.minHeight
        val topY = grid.maxHeight - grid.minHeight - 1
        for (r in 1..MAX_OPENNESS) {
            val blocked = blockedIn(prefix, maxOf(0, x - r), localY, maxOf(0, z - r), minOf(15, x + r), minOf(topY, localY + r), minOf(15, z + r))
            if (blocked > 0) return r - 1
        }
        return MAX_OPENNESS
    }

    fun scan(grid: ChunkGrid, samplesPerChunk: Int, random: Random, scannedAt: Long = 0, prefix: IntArray = IntArray(prefixSize(grid))): ChunkSurface {
        check(grid.maxHeight - grid.minHeight <= SurfacePos.MAX_HEIGHT_RANGE) {
            "World height ${grid.maxHeight - grid.minHeight} does not fit the surface index position packing"
        }
        buildBlockedPrefix(grid, prefix)
        val histogram = IntArray(MAX_OPENNESS + 1)
        val reservoir = IntArray(samplesPerChunk.coerceAtLeast(0))
        var seen = 0
        for (x in 0..15) for (z in 0..15) for (y in grid.minHeight + 1 until grid.maxHeight) {
            if (!isStandable(grid, x, y, z)) continue
            val openness = openness(grid, x, y, z, prefix)
            histogram[openness]++
            if (openness < 1) continue
            val packed = SurfacePos.pack(x, z, y - grid.minHeight, openness)
            if (seen < reservoir.size) reservoir[seen] = packed
            else {
                val slot = random.nextInt(seen + 1)
                if (slot < reservoir.size) reservoir[slot] = packed
            }
            seen++
        }
        return ChunkSurface(histogram, reservoir.copyOf(minOf(seen, reservoir.size)), VERSION, scannedAt)
    }

    private fun prefixIndex(x: Int, y: Int, z: Int) = (y * 17 + z) * 17 + x

    private fun blockedIn(prefix: IntArray, x0: Int, y0: Int, z0: Int, x1: Int, y1: Int, z1: Int): Int {
        val ax = x1 + 1
        val ay = y1 + 1
        val az = z1 + 1
        return prefix[prefixIndex(ax, ay, az)] -
                prefix[prefixIndex(x0, ay, az)] - prefix[prefixIndex(ax, y0, az)] - prefix[prefixIndex(ax, ay, z0)] +
                prefix[prefixIndex(x0, y0, az)] + prefix[prefixIndex(x0, ay, z0)] + prefix[prefixIndex(ax, y0, z0)] -
                prefix[prefixIndex(x0, y0, z0)]
    }
}
