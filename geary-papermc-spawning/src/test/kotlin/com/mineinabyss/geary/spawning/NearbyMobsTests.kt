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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

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

    @Test
    fun `cap counts match a brute force count`() {
        val random = Random(42)
        val categories = listOf(hostile, passive, SpawnCategory("water"))
        repeat(20) { round ->
            val snapshot = snapshot()
            val mobs = List(random.nextInt(50, 600)) {
                // A mix of mobs spread over a wide area and clustered on cell borders, some on fractional coordinates
                val spread = if (random.nextBoolean()) 600.0 else 70.0
                Mob(
                    categories.random(random),
                    random.nextDouble(-spread, spread) + if (round % 2 == 0) 0.0 else 0.5,
                    random.nextDouble(-200.0, 200.0),
                    random.nextDouble(-spread, spread),
                )
            }
            mobs.forEach { snapshot.add("mm:a", it.category, it.x, it.y, it.z) }
            repeat(30) {
                val x = random.nextDouble(-300.0, 300.0)
                val y = random.nextDouble(-150.0, 150.0)
                val z = random.nextDouble(-300.0, 300.0)
                val radius = listOf(32.0, 64.0, 128.0, 220.0, random.nextDouble(1.0, 300.0)).random(random)
                val expected = mobs
                    .filter { abs(it.x - x) <= radius && abs(it.y - y) <= radius && abs(it.z - z) <= radius }
                    .groupingBy { it.category }.eachCount()

                snapshot.around(x, y, z, radius).categoryCounts shouldBe expected
            }
        }
    }

    @Test
    fun `whole cells on the cube faces are counted exactly`() {
        val snapshot = snapshot().apply {
            add("mm:a", hostile, 0.0, 0.0, 0.0)
            add("mm:a", hostile, 31.999, 0.0, 31.999)
            add("mm:a", hostile, 32.0, 0.0, 0.0)
            add("mm:a", hostile, 0.0, 64.0, 0.0)
        }

        snapshot.around(0.0, 0.0, 0.0, 32.0).categoryCounts shouldBe mapOf(hostile to 3)
        snapshot.around(32.0, 32.0, 32.0, 32.0).categoryCounts shouldBe mapOf(hostile to 4)
        snapshot.around(-32.0, 0.0, -32.0, 64.0).categoryCounts shouldBe mapOf(hostile to 4)
    }

    @Test
    fun `slabs on the cube's top and bottom faces are counted exactly`() {
        val snapshot = snapshot().apply {
            for (y in -64..320 step 8) add("mm:a", hostile, 100.0, y.toDouble(), 100.0)
            add("mm:a", hostile, 100.0, 15.999, 100.0)
            add("mm:a", hostile, 100.0, 16.0, 100.0)
        }
        val ys = (-64..320 step 8).map { it.toDouble() } + listOf(15.999, 16.0)

        for (centerY in listOf(-300.0, -64.0, 0.0, 16.0, 47.5, 100.0, 236.0, 500.0)) {
            for (radius in listOf(16.0, 32.0, 220.0)) {
                val expected = ys.count { abs(it - centerY) <= radius }
                snapshot.around(100.0, centerY, 100.0, radius).categoryCounts shouldBe
                    (if (expected > 0) mapOf(hostile to expected) else emptyMap())
                snapshot.countNear(listOf("mm:a"), 100.0, centerY, 100.0, radius, Int.MAX_VALUE) shouldBe expected
            }
        }
    }

    @Test
    fun `parts cut by the cube faces match a brute force count on block aligned faces`() {
        val random = Random(7)
        val categories = listOf(hostile, passive)
        repeat(10) {
            val snapshot = snapshot()
            val mobs = List(random.nextInt(100, 800)) {
                // Whole and near whole coordinates land exactly on part and cube face borders
                val offset = listOf(0.0, 0.999, 0.5, 1e-9).random(random)
                Mob(
                    categories.random(random),
                    random.nextInt(-200, 200) + offset,
                    random.nextInt(-100, 100) + offset,
                    random.nextInt(-200, 200) + offset,
                )
            }
            mobs.forEach { snapshot.add("mm:a", it.category, it.x, it.y, it.z) }
            repeat(40) {
                val x = random.nextInt(-150, 150).toDouble()
                val y = random.nextInt(-50, 50).toDouble()
                val z = random.nextInt(-150, 150).toDouble()
                val radius = listOf(16.0, 31.0, 48.0, 64.0, 100.0, 128.0).random(random)
                val inCube = mobs.filter { abs(it.x - x) <= radius && abs(it.y - y) <= radius && abs(it.z - z) <= radius }

                snapshot.around(x, y, z, radius).categoryCounts shouldBe inCube.groupingBy { it.category }.eachCount()
                snapshot.countNear(listOf("mm:a"), x, y, z, radius, Int.MAX_VALUE) shouldBe inCube.size
            }
        }
    }

    @Test
    fun `deferred adds count the same as direct adds`() {
        val random = Random(9)
        val categories = listOf(hostile, passive)
        repeat(10) {
            val direct = snapshot()
            val deferred = snapshot()
            val base = List(random.nextInt(50, 400)) { randomMob(random, categories) }
            base.forEach {
                direct.add("mm:a", it.category, it.x, it.y, it.z)
                deferred.add("mm:a", it.category, it.x, it.y, it.z)
            }
            deferred.deferAdds()
            val spawned = List(random.nextInt(1, 40)) { randomMob(random, categories) }
            spawned.forEach {
                direct.add("mm:b", it.category, it.x, it.y, it.z)
                deferred.add("mm:b", it.category, it.x, it.y, it.z)
            }
            val all = base + spawned
            repeat(20) {
                val x = random.nextDouble(-200.0, 200.0)
                val y = random.nextDouble(-100.0, 100.0)
                val z = random.nextDouble(-200.0, 200.0)
                val radius = listOf(32.0, 128.0, 220.0).random(random)
                val inCube = all.filter { abs(it.x - x) <= radius && abs(it.y - y) <= radius && abs(it.z - z) <= radius }
                val expected = inCube.groupingBy { it.category }.eachCount()
                val spawnedInCube = spawned.count { it in inCube }

                deferred.around(x, y, z, radius).categoryCounts shouldBe expected
                deferred.nearby(deferred.cellCounts(x, y, z, radius), x, y, z, radius).categoryCounts shouldBe expected
                direct.around(x, y, z, radius).categoryCounts shouldBe expected
                deferred.countNear(listOf("mm:b"), x, y, z, radius, Int.MAX_VALUE) shouldBe spawnedInCube
                deferred.countNear(listOf("mm:a", "mm:b"), x, y, z, radius, Int.MAX_VALUE) shouldBe inCube.size
                deferred.countNear(listOf("mm:a", "mm:b"), x, y, z, radius, stopAt = 3) shouldBe minOf(inCube.size, 3)
            }
            deferred.applyDeferredAdds()
            repeat(10) {
                val x = random.nextDouble(-200.0, 200.0)
                val z = random.nextDouble(-200.0, 200.0)
                deferred.around(x, 0.0, z, 128.0).categoryCounts shouldBe direct.around(x, 0.0, z, 128.0).categoryCounts
                deferred.countNear(listOf("mm:b"), x, 0.0, z, 128.0, Int.MAX_VALUE) shouldBe
                    direct.countNear(listOf("mm:b"), x, 0.0, z, 128.0, Int.MAX_VALUE)
            }
        }
    }

    @Test
    fun `cell counts on other threads ignore adds deferred meanwhile`() {
        val random = Random(13)
        val snapshot = snapshot()
        val mobs = List(2000) { randomMob(random, listOf(hostile, passive)) }
        mobs.forEach { snapshot.add("mm:a", it.category, it.x, it.y, it.z) }
        val centers = List(400) { Triple(random.nextDouble(-200.0, 200.0), random.nextDouble(-100.0, 100.0), random.nextDouble(-200.0, 200.0)) }
        val expected = centers.map { (x, y, z) -> snapshot.cellCounts(x, y, z, 128.0) }

        snapshot.deferAdds()
        val executor = Executors.newFixedThreadPool(4)
        try {
            val futures = centers.map { (x, y, z) -> executor.submit<HashMap<SpawnCategory, Int>> { snapshot.cellCounts(x, y, z, 128.0) } }
            repeat(500) {
                val mob = randomMob(random, listOf(hostile, passive, SpawnCategory("water")))
                snapshot.add("mm:b", mob.category, mob.x, mob.y, mob.z)
            }
            futures.map { it.get(30, TimeUnit.SECONDS) } shouldBe expected
        } finally {
            executor.shutdownNow()
        }
        snapshot.applyDeferredAdds()
        snapshot.countNear(listOf("mm:b"), 0.0, 0.0, 0.0, 10_000.0, Int.MAX_VALUE) shouldBe 500
    }

    private fun randomMob(random: Random, categories: List<SpawnCategory>) = Mob(
        categories.random(random),
        random.nextDouble(-300.0, 300.0),
        random.nextDouble(-150.0, 150.0),
        random.nextDouble(-300.0, 300.0),
    )

    private data class Mob(val category: SpawnCategory, val x: Double, val y: Double, val z: Double)
}
