package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.spawning.conditions.IsOpenAreaCondition
import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkRect
import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkSurface
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadPos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSampling
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfacePos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKey
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKeyX
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SpreadSamplingTests {
    private val minHeight = -64
    private val rect = ChunkRect(-10, -10, 10, 10)

    private fun surface(openness: Int, count: Int, vararg ys: Int) = ChunkSurface(
        histogram = IntArray(8).also { it[openness] = count },
        positions = ys.mapIndexed { i, y -> SurfacePos.pack(i % 16, i / 16, y - minHeight, openness) }.toIntArray(),
    )

    @Test
    fun `distSq should scale only the vertical axis`() {
        SpreadSampling.distSq(SpreadPos(0, 0, 0), SpreadPos(3, 0, 4), 1.0) shouldBe 25.0
        SpreadSampling.distSq(SpreadPos(0, 0, 0), SpreadPos(0, 4, 0), 0.5) shouldBe 4.0
        SpreadSampling.distSq(SpreadPos(0, 0, 0), SpreadPos(0, 100, 0), 0.0) shouldBe 0.0
    }

    @Test
    fun `spacedCandidates should match a brute force filter`() {
        val random = Random(3)
        fun randomPos() = SpreadPos(random.nextInt(-200, 200), random.nextInt(-60, 60), random.nextInt(-200, 200))
        repeat(20) {
            val candidates = List(30) { randomPos() }
            val existing = List(15) { randomPos() }
            val minDistSq = 60.0 * 60.0

            val expected = candidates.filter { c -> existing.minOf { SpreadSampling.distSq(c, it, 0.5) } >= minDistSq }

            SpreadSampling.spacedCandidates(candidates, existing, 0.5, minDistSq) shouldBe expected
        }
    }

    @Test
    fun `spacedCandidates should keep draw order instead of favouring the largest gap`() {
        val candidates = listOf(SpreadPos(20, 0, 0), SpreadPos(500, 0, 0), SpreadPos(40, 0, 0))
        SpreadSampling.spacedCandidates(candidates, listOf(SpreadPos(0, 0, 0)), 1.0, 10.0 * 10.0) shouldBe candidates
    }

    @Test
    fun `spacedCandidates should reject candidates closer than the minimum`() {
        SpreadSampling.spacedCandidates(listOf(SpreadPos(0, 0, 0)), listOf(SpreadPos(5, 0, 0)), 1.0, 36.0).shouldBeEmpty()
    }

    @Test
    fun `scatteredChunks should visit every chunk of every area exactly once`() {
        val rects = listOf(ChunkRect(-3, -2, 4, 6), ChunkRect(100, 100, 100, 104), ChunkRect(0, 0, 30, 30))
        val visited = SpreadSampling.scatteredChunks(rects).asSequence().toList()
        val expected = rects.flatMap { r -> (r.minX..r.maxX).flatMap { x -> (r.minZ..r.maxZ).map { z -> chunkKey(x, z) } } }

        visited.size shouldBe expected.size
        visited.toSet() shouldBe expected.toSet()
    }

    @Test
    fun `scatteredChunks should alternate between areas`() {
        val rects = listOf(ChunkRect(0, 0, 9, 9), ChunkRect(1000, 1000, 1009, 1009))
        val firstTen = SpreadSampling.scatteredChunks(rects).asSequence().take(10).toList()
        firstTen.count { chunkKeyX(it) < 1000 } shouldBe 5
    }

    @Test
    fun `weightedIndex should pick proportionally to weight`() {
        val random = Random(11)
        val cumulative = doubleArrayOf(1.0, 3.0, 6.0)
        val counts = IntArray(3)
        val draws = 60_000
        repeat(draws) { counts[SpreadSampling.weightedIndex(cumulative, random)]++ }

        (counts[0].toDouble() / draws) shouldBe (1.0 / 6 plusOrMinus 0.01)
        (counts[1].toDouble() / draws) shouldBe (2.0 / 6 plusOrMinus 0.01)
        (counts[2].toDouble() / draws) shouldBe (3.0 / 6 plusOrMinus 0.01)
    }

    @Test
    fun `drawCandidates should favour chunks with more surface`() {
        val chunks = mapOf(
            chunkKey(0, 0) to surface(4, 100, 10),
            chunkKey(1, 0) to surface(4, 300, 10),
        )
        val random = Random(5)
        var inSecond = 0
        val draws = 20_000
        repeat(draws) {
            val pos = SpreadSampling.drawCandidates(rect, chunks::get, minHeight, -64, 64, 0, { _, _, _ -> false }, 1, random).single()
            if (pos.chunkX == 1) inSecond++
        }
        (inSecond.toDouble() / draws) shouldBe (0.75 plusOrMinus 0.02)
    }

    @Test
    fun `drawCandidates should filter by y, exclusion and openness`() {
        val chunks = mapOf(
            chunkKey(0, 0) to surface(4, 10, 10, 50),
            chunkKey(2, 3) to surface(2, 10, 20),
        )
        val random = Random(9)

        val inRange = SpreadSampling.drawCandidates(rect, chunks::get, minHeight, 0, 30, 0, { _, _, _ -> false }, 10, random)
        inRange.map { it.y }.toSet() shouldBe setOf(10, 20)

        val excluded = SpreadSampling.drawCandidates(rect, chunks::get, minHeight, -64, 64, 0, { x, y, _ -> y == 50 || x shr 4 == 2 }, 10, random)
        excluded.map { it.y }.toSet() shouldBe setOf(10)

        SpreadSampling.drawCandidates(ChunkRect(1, 0, 5, 5), chunks::get, minHeight, -64, 64, 0, { _, _, _ -> false }, 10, random)
            .map { it.chunkX to it.chunkZ }.toSet() shouldBe setOf(2 to 3)

        val open = SpreadSampling.drawCandidates(rect, chunks::get, minHeight, -64, 64, 3, { _, _, _ -> false }, 10, random)
        open.all { it.chunkX == 0 && it.chunkZ == 0 } shouldBe true
        open.map { it.y }.toSet() shouldBe setOf(10, 50)
    }

    @Test
    fun `drawCandidates should place positions at absolute block coordinates`() {
        val chunks = mapOf(chunkKey(-2, 3) to surface(4, 1, -30))
        SpreadSampling.drawCandidates(rect, chunks::get, minHeight, -64, 64, 0, { _, _, _ -> false }, 1, Random(1)) shouldBe listOf(SpreadPos(-32, -30, 48))
    }

    @Test
    fun `chunk weight should skip openness zero since it is never sampled`() {
        val surface = ChunkSurface(intArrayOf(100, 5, 0, 0, 0, 0, 0, 3), IntArray(0))
        SpreadSampling.chunkWeight(surface, 0, 2, 1) shouldBe 4.0
        SpreadSampling.chunkWeight(surface, 7, 2, 2) shouldBe 3.0
        SpreadSampling.chunkWeight(surface, 0, 0, 0) shouldBe 0.0
    }

    @Test
    fun `surfaceStats should count chunks in the rectangle and share the draw weight`() {
        val chunks = mapOf(chunkKey(0, 0) to surface(4, 100, 10, 50), chunkKey(20, 0) to surface(4, 100, 10))
        val stats = SpreadSampling.surfaceStats(rect, chunks::get, minHeight, 0..30, 0)
        stats.indexedChunks shouldBe 1
        stats.weight shouldBe 50.0
    }

    @Test
    fun `requiredOpenness should take the largest open area range`() {
        val other = object : Condition {
            override fun ActionGroupContext.execute() = true
        }
        SpreadSampling.requiredOpenness(listOf(IsOpenAreaCondition(4), other, IsOpenAreaCondition(2))) shouldBe 4
        SpreadSampling.requiredOpenness(emptyList()) shouldBe 0
    }
}
