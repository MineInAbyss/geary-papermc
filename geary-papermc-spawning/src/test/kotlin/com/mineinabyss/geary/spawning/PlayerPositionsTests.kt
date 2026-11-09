package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.choosing.PlayerPositions
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import kotlin.random.Random

class PlayerPositionsTests {
    private val world = mockk<World>()
    private val otherWorld = mockk<World>()

    private fun player(world: World, x: Double, y: Double, z: Double) = mockk<Player> {
        every { getLocation(any()) } answers {
            firstArg<Location>().apply {
                this.world = world
                this.x = x
                this.y = y
                this.z = z
            }
        }
    }

    @Test
    fun `finds players across cell borders including negative coordinates`() {
        val positions = PlayerPositions(listOf(player(world, -1.0, 64.0, -1.0)))

        positions.anyWithin(Location(world, 1.0, 64.0, 1.0), 400.0) shouldBe true
        positions.anyWithin(Location(world, 18.0, 64.0, -1.0), 400.0) shouldBe true
        positions.anyWithin(Location(world, -40.0, 64.0, -1.0), 400.0) shouldBe false
    }

    @Test
    fun `uses full 3d distance`() {
        val positions = PlayerPositions(listOf(player(world, 0.0, 0.0, 0.0)))

        positions.anyWithin(Location(world, 0.0, 19.0, 0.0), 400.0) shouldBe true
        positions.anyWithin(Location(world, 0.0, 21.0, 0.0), 400.0) shouldBe false
    }

    @Test
    fun `ignores players in other worlds`() {
        val positions = PlayerPositions(listOf(player(otherWorld, 0.0, 0.0, 0.0)))

        positions.anyWithin(Location(world, 0.0, 0.0, 0.0), 400.0) shouldBe false
        positions.anyWithin(Location(otherWorld, 0.0, 0.0, 0.0), 400.0) shouldBe true
    }

    @Test
    fun `matches a brute force search with players on colliding cells`() {
        val random = Random(7)
        // Cells with cellX == cellZ or cellX == -cellZ share Long.hashCode, so a map that does not mix the bits piles them into one bin
        val players = List(500) {
            val cell = random.nextInt(-40, 40)
            val sign = if (random.nextBoolean()) 1 else -1
            Triple(cell * 32.0 + random.nextDouble(32.0), random.nextDouble(-64.0, 64.0), sign * cell * 32.0 + random.nextDouble(32.0))
        }
        val positions = PlayerPositions()
        players.forEach { (x, y, z) -> positions.add(world, x, y, z) }

        repeat(2000) {
            val x = random.nextDouble(-1300.0, 1300.0)
            val y = random.nextDouble(-80.0, 80.0)
            val z = random.nextDouble(-1300.0, 1300.0)
            val distanceSq = random.nextDouble(1.0, 60.0).let { it * it }
            val expected = players.any { (px, py, pz) -> (px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z) < distanceSq }

            positions.anyWithin(Location(world, x, y, z), distanceSq) shouldBe expected
        }
    }
}
