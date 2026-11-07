package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkGrid
import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkSurface
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceFlags.FLOOR
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceFlags.LIQUID
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceFlags.PASSABLE
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfacePos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceScanner
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKey
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKeyX
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKeyZ
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class SurfaceScannerTests {
    private val air = PASSABLE
    private val stone = FLOOR

    private fun grid(minHeight: Int = -64, maxHeight: Int = 64, flags: (x: Int, y: Int, z: Int) -> Int) =
        ChunkGrid.build(minHeight, maxHeight, { false }, flags)

    private fun flatFloor(floorY: Int = 0) = grid { _, y, _ -> if (y <= floorY) stone else air }

    private fun ChunkSurface.standable() = histogram.sum()

    // Brute force copy of IsOpenAreaCondition, which clamps x and z to the chunk and y to the world
    private fun referenceOpenness(grid: ChunkGrid, x: Int, y: Int, z: Int): Int {
        fun open(range: Int): Boolean {
            for (dx in -range..range) for (dy in 0..range) for (dz in -range..range) {
                val cy = (y + dy).coerceIn(grid.minHeight, grid.maxHeight)
                if (grid.flagsAt((x + dx).coerceIn(0, 15), cy, (z + dz).coerceIn(0, 15)) and PASSABLE == 0) return false
            }
            return true
        }
        return (0..SurfaceScanner.MAX_OPENNESS).lastOrNull { open(it) } ?: -1
    }

    @Test
    fun `flat floor should be fully open everywhere since reads are clamped to the chunk`() {
        val grid = flatFloor()
        val surface = SurfaceScanner.scan(grid, 1000, Random(1))

        surface.histogram[SurfaceScanner.MAX_OPENNESS] shouldBe 256
        surface.standable() shouldBe 256
        surface.positions.size shouldBe 256
        surface.positions.all { -64 + SurfacePos.yOffset(it) == 1 } shouldBe true
    }

    @Test
    fun `walls should cap openness`() {
        val grid = grid { x, y, _ -> if (y <= 0 || x == 10 && y <= 10) stone else air }

        SurfaceScanner.openness(grid, 5, 1, 5) shouldBe 4
        SurfaceScanner.openness(grid, 9, 1, 5) shouldBe 0
        SurfaceScanner.openness(grid, 11, 1, 5) shouldBe 0
        SurfaceScanner.openness(grid, 10, 11, 5) shouldBe SurfaceScanner.MAX_OPENNESS
    }

    @Test
    fun `openness should match IsOpenAreaCondition on random terrain`() {
        val random = Random(7)
        val grid = grid(minHeight = 0, maxHeight = 32) { _, y, _ ->
            if (y == 0 || random.nextInt(12) == 0) stone else air
        }
        for (x in 0..15) for (z in 0..15) for (y in 1 until 32) {
            if (!SurfaceScanner.isStandable(grid, x, y, z)) continue
            SurfaceScanner.openness(grid, x, y, z) shouldBe referenceOpenness(grid, x, y, z)
        }
    }

    @Test
    fun `openness near the world top should treat out of world blocks as open`() {
        val grid = grid(minHeight = 0, maxHeight = 16) { _, y, _ -> if (y == 0 || y == 13) stone else air }
        SurfaceScanner.openness(grid, 8, 14, 8) shouldBe referenceOpenness(grid, 8, 14, 8)
        SurfaceScanner.openness(grid, 8, 14, 8) shouldBe SurfaceScanner.MAX_OPENNESS
    }

    @Test
    fun `liquid or waterlogged feet should not be standable`() {
        val water = grid { _, y, _ -> if (y <= 0) stone else if (y == 1) PASSABLE or LIQUID else air }
        SurfaceScanner.isStandable(water, 3, 1, 3) shouldBe false

        val waterloggedSlab = grid { _, y, _ -> if (y <= 0) stone else if (y == 1) FLOOR or LIQUID else air }
        SurfaceScanner.isStandable(waterloggedSlab, 3, 1, 3) shouldBe false
        SurfaceScanner.isStandable(waterloggedSlab, 3, 2, 3) shouldBe true
    }

    @Test
    fun `passable floor blocks like snow layers should be standable from above`() {
        val grid = grid { _, y, _ -> if (y < 0) stone else if (y == 0) FLOOR or PASSABLE else air }
        SurfaceScanner.isStandable(grid, 3, 1, 3) shouldBe true
        SurfaceScanner.openness(grid, 3, 1, 3) shouldBe SurfaceScanner.MAX_OPENNESS
    }

    @Test
    fun `stacked ledges in one column should all be counted`() {
        val grid = grid { _, y, _ -> if (y == 0 || y == 20 || y == 40) stone else air }
        val surface = SurfaceScanner.scan(grid, 1000, Random(1))

        surface.standable() shouldBe 3 * 256
        surface.positions.map { -64 + SurfacePos.yOffset(it) }.toSet() shouldBe setOf(1, 21, 41)
    }

    @Test
    fun `negative y should pack and unpack relative to min height`() {
        val grid = grid(minHeight = -256, maxHeight = 64) { _, y, _ -> if (y <= -201) stone else air }
        val surface = SurfaceScanner.scan(grid, 1000, Random(1))

        surface.positions.all { -256 + SurfacePos.yOffset(it) == -200 } shouldBe true
    }

    @Test
    fun `reservoir should respect the sample cap and be deterministic with a seed`() {
        val grid = flatFloor()
        SurfaceScanner.scan(grid, 10, Random(1)).positions.size shouldBe 10
        SurfaceScanner.scan(grid, 10, Random(42)).positions.toList() shouldBe SurfaceScanner.scan(grid, 10, Random(42)).positions.toList()
    }

    @Test
    fun `positions should only keep openness of at least one while the histogram counts all`() {
        val grid = grid { x, y, _ -> if (y <= 0 || x % 2 == 0 && y == 2) stone else air }
        val surface = SurfaceScanner.scan(grid, 1000, Random(1))

        surface.histogram[0] shouldBe 256
        surface.histogram[SurfaceScanner.MAX_OPENNESS] shouldBe 128
        surface.positions.size shouldBe 128
        surface.positions.all { SurfacePos.openness(it) >= 1 } shouldBe true
    }

    @Test
    fun `empty sections should be filled as air without reading them`() {
        var reads = 0
        val grid = ChunkGrid.build(-64, 64, { it == 0 }) { _, _, _ -> reads++; stone }

        grid.flagsAt(0, -64, 0) shouldBe PASSABLE
        grid.flagsAt(0, -49, 0) shouldBe PASSABLE
        grid.flagsAt(0, -48, 0) shouldBe stone
        reads shouldBe 256 * (128 - 16)
    }

    @Test
    fun `worlds too tall for the packing should fail loudly`() {
        val grid = grid(minHeight = 0, maxHeight = SurfacePos.MAX_HEIGHT_RANGE + 16) { _, _, _ -> air }
        shouldThrow<IllegalStateException> { SurfaceScanner.scan(grid, 10, Random(1)) }
    }

    @Test
    fun `needsScan should rescan old or outdated surfaces`() {
        val surface = ChunkSurface(IntArray(8), IntArray(0), SurfaceScanner.VERSION, scannedAt = 1_000)
        surface.needsScan(1_000 + 3.days.inWholeSeconds, 3.days) shouldBe false
        surface.needsScan(1_001 + 3.days.inWholeSeconds, 3.days) shouldBe true
        ChunkSurface(IntArray(8), IntArray(0), SurfaceScanner.VERSION - 1, scannedAt = 1_000).needsScan(1_000, 3.days) shouldBe true
    }

    @Test
    fun `packing should round trip`() {
        val packed = SurfacePos.pack(15, 7, 4095, 7)
        SurfacePos.lx(packed) shouldBe 15
        SurfacePos.lz(packed) shouldBe 7
        SurfacePos.yOffset(packed) shouldBe 4095
        SurfacePos.openness(packed) shouldBe 7

        val key = chunkKey(-3, 12345)
        chunkKeyX(key) shouldBe -3
        chunkKeyZ(key) shouldBe 12345
    }

    @Test
    fun `blobs should round trip and saturate the histogram`() {
        val surface = ChunkSurface(intArrayOf(0, 1, 2, 3, 4, 5, 70_000, 65_535), intArrayOf(SurfacePos.pack(1, 2, 3, 4), -1), 1, 99)
        val decoded = ChunkSurface.decode(surface.encodeHistogram(), surface.encodePositions(), 1, 99)

        decoded.histogram.toList() shouldBe listOf(0, 1, 2, 3, 4, 5, 65_535, 65_535)
        decoded.positions.toList() shouldBe surface.positions.toList()
        decoded.scannedAt shouldBe 99
    }

    @Test
    fun `removing a position should ignore its openness`() {
        val surface = ChunkSurface(IntArray(8), intArrayOf(SurfacePos.pack(1, 2, 3, 4), SurfacePos.pack(5, 6, 7, 1)))
        surface.withoutPosition(1, 2, 3).positions.toList() shouldBe listOf(SurfacePos.pack(5, 6, 7, 1))
        surface.withoutPosition(9, 9, 9).positions.size shouldBe 2
    }
}
