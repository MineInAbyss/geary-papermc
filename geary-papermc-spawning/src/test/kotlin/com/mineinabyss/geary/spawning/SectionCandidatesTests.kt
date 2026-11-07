package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.choosing.SectionCandidates
import com.mineinabyss.geary.papermc.spawning.choosing.SectionCandidates.FLOOR
import com.mineinabyss.geary.papermc.spawning.choosing.SectionCandidates.GROUND_FEET
import com.mineinabyss.geary.papermc.spawning.choosing.SectionCandidates.WATER
import io.kotest.matchers.shouldBe
import net.minecraft.util.SimpleBitStorage
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SectionCandidatesTests {
    // Air, stone, water, a waterloggable slab that is also a floor, a snow layer, a passable floor like scaffolding, lava
    private val palette = intArrayOf(GROUND_FEET, FLOOR, WATER, WATER or FLOOR, GROUND_FEET or FLOOR, GROUND_FEET or FLOOR, 0)

    private fun isSet(bits: LongArray, x: Int, y: Int, z: Int): Boolean {
        if (bits.isEmpty()) return false
        val index = (y shl 8) or (z shl 4) or x
        return bits[index ushr 6] and (1L shl index) != 0L
    }

    @Test
    fun `marks water and passable blocks on a floor, including the floor below the section`() {
        val random = Random(3)
        repeat(50) {
            val below = IntArray(256) { palette.random(random) }
            val section = IntArray(4096) { palette.random(random) }
            val bits = SectionCandidates.build(
                feetFlags = { x, y, z -> section[(y shl 8) or (z shl 4) or x] },
                belowFlags = { x, z -> below[(z shl 4) or x] },
            )

            for (y in 0..15) for (z in 0..15) for (x in 0..15) {
                val feet = section[(y shl 8) or (z shl 4) or x]
                val floor = if (y == 0) below[(z shl 4) or x] else section[((y - 1) shl 8) or (z shl 4) or x]
                val expected = feet and WATER != 0 || feet and GROUND_FEET != 0 && floor and FLOOR != 0
                isSet(bits, x, y, z) shouldBe expected
            }
        }
    }

    @Test
    fun `returns an empty array when nothing can spawn`() {
        val bits = SectionCandidates.build(feetFlags = { _, _, _ -> FLOOR }, belowFlags = { _, _ -> FLOOR })

        bits.size shouldBe 0
    }

    @Test
    fun `air section only marks its bottom layer above floors`() {
        val bits = SectionCandidates.build(feetFlags = { _, _, _ -> GROUND_FEET }, belowFlags = { x, _ -> if (x == 3) FLOOR else 0 })

        for (y in 0..15) for (z in 0..15) for (x in 0..15) isSet(bits, x, y, z) shouldBe (y == 0 && x == 3)
    }

    @Test
    fun `air sections match the full build`() {
        val random = Random(5)
        repeat(50) {
            val below = IntArray(256) { palette.random(random) }
            val expected = SectionCandidates.build(feetFlags = { _, _, _ -> GROUND_FEET }, belowFlags = { x, z -> below[(z shl 4) or x] })
            val bits = SectionCandidates.buildAir { x, z -> below[(z shl 4) or x] }

            for (y in 0..15) for (z in 0..15) for (x in 0..15) isSet(bits, x, y, z) shouldBe isSet(expected, x, y, z)
        }
        SectionCandidates.buildAir { _, _ -> GROUND_FEET }.size shouldBe 0
    }

    @Test
    fun `packed ids decode like SimpleBitStorage`() {
        val random = Random(11)
        for (bits in 1..15) {
            val storage = SimpleBitStorage(bits, 4096)
            val values = IntArray(4096) { random.nextInt(1 shl bits) }
            values.forEachIndexed { i, v -> storage.set(i, v) }
            for ((from, count) in listOf(0 to 4096, 3840 to 256, 1 to 4095, random.nextInt(4096).let { it to 4096 - it })) {
                val decoded = IntArray(count)
                SectionCandidates.forEachPacked(storage.raw, bits, from, count) { i, v -> decoded[i] = v }

                decoded.toList() shouldBe (from until from + count).map { storage.get(it) }
            }
        }
    }
}
