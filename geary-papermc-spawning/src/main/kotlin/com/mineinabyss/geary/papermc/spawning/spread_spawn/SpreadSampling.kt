package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.spawning.conditions.IsOpenAreaCondition
import kotlin.random.Random

data class SpreadPos(val x: Int, val y: Int, val z: Int) {
    val chunkX get() = x shr 4
    val chunkZ get() = z shr 4
    val chunkKey get() = chunkKey(chunkX, chunkZ)
}

data class ChunkRect(val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int) {
    fun contains(cx: Int, cz: Int) = cx in minX..maxX && cz in minZ..maxZ
}

data class SurfaceStats(val indexedChunks: Int, val weight: Double)

object SpreadSampling {
    fun distSq(a: SpreadPos, b: SpreadPos, verticalWeight: Double): Double {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y) * verticalWeight
        val dz = (a.z - b.z).toDouble()
        return dx * dx + dz * dz + dy * dy
    }

    fun spacedCandidates(
        candidates: List<SpreadPos>,
        existing: List<SpreadPos>,
        verticalWeight: Double,
        minDistSq: Double,
    ): List<SpreadPos> = candidates.filter { candidate ->
        existing.none { distSq(candidate, it, verticalWeight) < minDistSq }
    }

    fun weightedIndex(cumulative: DoubleArray, random: Random): Int {
        val target = random.nextDouble() * cumulative.last()
        var low = 0
        var high = cumulative.lastIndex
        while (low < high) {
            val mid = (low + high) ushr 1
            if (cumulative[mid] > target) high = mid else low = mid + 1
        }
        return low
    }

    /** Openness 0 is never sampled, so it is left out of the weight even when [minOpenness] allows it */
    fun chunkWeight(surface: ChunkSurface, minOpenness: Int, eligible: Int, passing: Int): Double {
        if (eligible == 0 || passing == 0) return 0.0
        var surfaceCount = 0
        for (o in maxOf(minOpenness, 1)..SurfaceScanner.MAX_OPENNESS) surfaceCount += surface.histogram[o]
        return surfaceCount.toDouble() * passing / eligible
    }

    fun drawCandidates(
        rect: ChunkRect,
        surfaceAt: (chunkKey: Long) -> ChunkSurface?,
        minHeight: Int,
        minY: Int,
        maxY: Int,
        minOpenness: Int,
        excluded: (x: Int, y: Int, z: Int) -> Boolean,
        count: Int,
        random: Random,
    ): List<SpreadPos> {
        if (count <= 0) return emptyList()
        val chunkKeys = mutableListOf<Long>()
        val passingByChunk = mutableListOf<IntArray>()
        val weights = mutableListOf<Double>()
        val minYOffset = minY - minHeight
        val maxYOffset = maxY - minHeight

        for (cx in rect.minX..rect.maxX) for (cz in rect.minZ..rect.maxZ) {
            val key = chunkKey(cx, cz)
            val surface = surfaceAt(key) ?: continue
            val passing = IntArray(surface.positions.size)
            var eligible = 0
            var passingCount = 0

            for (packed in surface.positions) {
                if (SurfacePos.openness(packed) < minOpenness) continue
                eligible++
                val yOffset = SurfacePos.yOffset(packed)
                if (yOffset !in minYOffset..maxYOffset) continue
                if (excluded((cx shl 4) + SurfacePos.lx(packed), minHeight + yOffset, (cz shl 4) + SurfacePos.lz(packed))) continue
                passing[passingCount++] = packed
            }

            val weight = chunkWeight(surface, minOpenness, eligible, passingCount)
            if (weight <= 0.0) continue

            chunkKeys += key
            passingByChunk += passing.copyOf(passingCount)
            weights += weight
        }
        if (weights.isEmpty()) return emptyList()

        val cumulative = DoubleArray(weights.size)
        var sum = 0.0
        weights.forEachIndexed { i, w -> sum += w; cumulative[i] = sum }

        val chosen = LinkedHashSet<SpreadPos>()
        repeat(count * 4) {
            if (chosen.size >= count) return@repeat
            val chunk = weightedIndex(cumulative, random)
            val packed = passingByChunk[chunk].random(random)
            val key = chunkKeys[chunk]
            chosen += SpreadPos(
                (chunkKeyX(key) shl 4) + SurfacePos.lx(packed),
                minHeight + SurfacePos.yOffset(packed),
                (chunkKeyZ(key) shl 4) + SurfacePos.lz(packed),
            )
        }
        return chosen.toList()
    }

    fun surfaceStats(
        rect: ChunkRect,
        surfaceAt: (chunkKey: Long) -> ChunkSurface?,
        minHeight: Int,
        yRange: IntRange,
        minOpenness: Int
    ): SurfaceStats {
        var chunks = 0
        var weight = 0.0
        for (cx in rect.minX..rect.maxX) for (cz in rect.minZ..rect.maxZ) {
            val surface = surfaceAt(chunkKey(cx, cz)) ?: continue
            chunks++
            val eligible = surface.positions.count { SurfacePos.openness(it) >= minOpenness }
            val passing = surface.positions.count { SurfacePos.openness(it) >= minOpenness && minHeight + SurfacePos.yOffset(it) in yRange }
            weight += chunkWeight(surface, minOpenness, eligible, passing)
        }
        return SurfaceStats(chunks, weight)
    }

    // Takes one chunk from each area in turn, in a scattered order, so a walk that is only partly done still covers every area evenly
    fun scatteredChunks(rects: List<ChunkRect>): Iterator<Long> = iterator {
        val sizes = rects.map { (it.maxX - it.minX + 1).toLong() * (it.maxZ - it.minZ + 1) }
        for (i in 0 until (sizes.maxOrNull() ?: 0)) rects.forEachIndexed { r, rect ->
            if (i >= sizes[r]) return@forEachIndexed
            val n = i * SCATTER_PRIME % sizes[r]
            val depth = rect.maxZ - rect.minZ + 1
            yield(chunkKey(rect.minX + (n / depth).toInt(), rect.minZ + (n % depth).toInt()))
        }
    }

    fun requiredOpenness(conditions: Iterable<Condition>): Int =
        conditions.filterIsInstance<IsOpenAreaCondition>().maxOfOrNull { it.range } ?: 0

    // Prime above any area's chunk count, so stepping by it visits every chunk of an area exactly once
    private const val SCATTER_PRIME = 1_000_003L
}
