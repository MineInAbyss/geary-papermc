package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.WorldMobs
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Test
import java.util.UUID

class NearbyMobsTests {
    private val world = mockk<World> { every { uid } returns UUID.randomUUID() }
    private val otherWorld = mockk<World> { every { uid } returns UUID.randomUUID() }
    private val hostile = SpawnCategory("hostile")
    private val passive = SpawnCategory("passive")

    private fun snapshot() = WorldMobs(world.uid, tick = 0)

    @Test
    fun `counts only matching types inside the radius`() {
        val snapshot = snapshot().apply {
            add("mm:a", hostile, 10.0, 0.0, 0.0)
            add("mm:a", hostile, 30.0, 0.0, 0.0)
            add("mm:b", hostile, 5.0, 0.0, 0.0)
            add(null, hostile, 0.0, 0.0, 0.0)
        }
        val nearby = snapshot.around(0.0, 0.0, 0.0, radius = 100.0)

        nearby.countNear(listOf("mm:a"), Location(world, 0.0, 0.0, 0.0), radius = 20.0, stopAt = 10) shouldBe 1
        nearby.countNear(listOf("mm:a", "mm:b"), Location(world, 0.0, 0.0, 0.0), radius = 50.0, stopAt = 10) shouldBe 3
        nearby.categoryCounts[hostile] shouldBe 4
    }

    @Test
    fun `stops counting at the limit`() {
        val nearby = snapshot().apply { repeat(5) { add("mm:a", hostile, 0.0, 0.0, 0.0) } }.around(0.0, 0.0, 0.0, 100.0)

        nearby.countNear(listOf("mm:a"), Location(world, 0.0, 0.0, 0.0), radius = 10.0, stopAt = 2) shouldBe 2
    }

    @Test
    fun `refuses queries in another world`() {
        val nearby = snapshot().around(0.0, 0.0, 0.0, 100.0)

        nearby.countNear(listOf("mm:a"), Location(otherWorld, 0.0, 0.0, 0.0), radius = 10.0, stopAt = 1).shouldBeNull()
    }

    @Test
    fun `counts queries outside the cap cube from the whole world`() {
        val nearby = snapshot().apply { add("mm:a", hostile, 1000.0, 0.0, 0.0) }.around(0.0, 0.0, 0.0, 100.0)

        nearby.categoryCounts[hostile].shouldBeNull()
        nearby.countNear(listOf("mm:a"), Location(world, 990.0, 0.0, 0.0), radius = 50.0, stopAt = 10) shouldBe 1
    }

    @Test
    fun `cap counts use a cube around each center across cell borders`() {
        val snapshot = snapshot().apply {
            add("mm:a", hostile, -1.0, 0.0, -1.0)
            add("mm:a", hostile, 63.9, 0.0, 64.0)
            add("mm:a", hostile, -129.0, 0.0, 0.0)
            add("mm:a", passive, 0.0, 200.0, 0.0)
        }

        snapshot.around(0.0, 0.0, 0.0, 128.0).categoryCounts shouldBe mapOf(hostile to 2)
        snapshot.around(-128.0, 0.0, 0.0, 128.0).categoryCounts shouldBe mapOf(hostile to 2)
    }

    @Test
    fun `recorded spawns are seen by later players exactly once`() {
        val snapshot = snapshot().apply { add("mm:a", hostile, 0.0, 0.0, 0.0) }
        val first = snapshot.around(0.0, 0.0, 0.0, 100.0)
        first.add("mm:a", hostile, Location(world, 10.0, 0.0, 0.0))

        first.categoryCounts[hostile] shouldBe 2
        val second = snapshot.around(20.0, 0.0, 0.0, 100.0)
        second.categoryCounts[hostile] shouldBe 2
        second.countNear(listOf("mm:a"), Location(world, 0.0, 0.0, 0.0), radius = 50.0, stopAt = 10) shouldBe 2
    }
}
