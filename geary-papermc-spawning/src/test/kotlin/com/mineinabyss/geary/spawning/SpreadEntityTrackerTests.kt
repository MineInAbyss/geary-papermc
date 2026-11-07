package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadEntityTracker
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.StoredEntity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SpreadEntityTrackerTests {
    private val plugin = mockk<Plugin> {
        every { name } returns "geary"
        every { namespace() } returns "geary"
    }
    private val world = mockk<World>()
    private val spawned = mutableListOf<Entity>()
    private val tracker = SpreadEntityTracker(plugin)

    private fun entity(): Entity {
        val store = HashMap<NamespacedKey, Long>()
        val data = mockk<PersistentDataContainer> {
            every { get(any<NamespacedKey>(), any<PersistentDataType<Long, Long>>()) } answers { store[firstArg()] }
            every { set(any<NamespacedKey>(), any<PersistentDataType<Long, Long>>(), any<Long>()) } answers { store[firstArg()] = thirdArg() }
        }
        var valid = true
        var persistent = true
        return mockk {
            every { persistentDataContainer } returns data
            every { isPersistent } answers { persistent }
            every { isPersistent = any() } answers { persistent = firstArg() }
            every { isValid } answers { valid }
            every { remove() } answers { valid = false }
        }
    }

    private fun row(id: Long, created: Long, x: Double = 1.0) =
        SpreadSpawnLocation(id, StoredEntity("mm:test", "test", Instant.ofEpochSecond(created)), Location(world, x, 2.0, 3.0))

    private fun tag(entity: Entity, id: Long, created: Long) = entity.also {
        it.persistentDataContainer.set(NamespacedKey(plugin, "spread_spawn_id"), PersistentDataType.LONG, id)
        it.persistentDataContainer.set(NamespacedKey(plugin, "spread_spawn_created"), PersistentDataType.LONG, created)
    }

    @BeforeEach
    fun mockTypes() {
        mockkObject(SpawnType.Companion)
        every { SpawnType.getType(any()) } returns object : SpawnType {
            override val key = "mm:test"
            override val category = SpawnCategory("test")
            override fun spawnAt(location: Location) = entity().also { spawned += it }
        }
    }

    @AfterEach
    fun unmockTypes() = unmockkObject(SpawnType.Companion)

    @Test
    fun `spawnIfAbsent should not spawn a live row twice`() {
        val row = row(1, 10)
        tracker.spawnIfAbsent(row)
        tracker.spawnIfAbsent(row)

        spawned.size shouldBe 1
        tracker.tagOf(spawned.single()) shouldBe SpreadEntityTracker.Tag(1, 10)
        tracker.liveIds() shouldBe setOf(1L)
    }

    @Test
    fun `spawned entities should not be saved with their chunk`() {
        tracker.spawnIfAbsent(row(1, 10))

        spawned.single().isPersistent shouldBe false
    }

    @Test
    fun `spawnIfAbsent should respawn once the entity is gone`() {
        val row = row(1, 10)
        tracker.spawnIfAbsent(row)
        spawned.single().remove()

        tracker.isLive(row) shouldBe false
        tracker.spawnIfAbsent(row)
        spawned.size shouldBe 2
    }

    @Test
    fun `rows being deleted should not spawn`() = runTest {
        val row = row(1, 10)
        tracker.deleting(listOf(1L)) {
            tracker.spawnIfAbsent(row)
            spawned.size shouldBe 0
        }
        tracker.spawnIfAbsent(row)
        spawned.size shouldBe 1
    }

    @Test
    fun `a reused id should remove the orphan and spawn the new row`() {
        tracker.spawnIfAbsent(row(1, 10))
        val orphan = spawned.single()
        val newer = row(1, 20)

        tracker.isLive(newer) shouldBe false
        orphan.isValid shouldBe false
        tracker.spawnIfAbsent(newer)
        spawned.size shouldBe 2
        tracker.tagOf(spawned.last()) shouldBe SpreadEntityTracker.Tag(1, 20)
    }

    @Test
    fun `register should only accept entities tagged for the row and not already held`() {
        val row = row(1, 10)
        tracker.register(row, tag(entity(), 1, 99)) shouldBe false
        tracker.register(row, entity()) shouldBe false

        val first = tag(entity(), 1, 10)
        tracker.register(row, first) shouldBe true
        tracker.register(row, first) shouldBe true
        tracker.register(row, tag(entity(), 1, 10)) shouldBe false
        tracker.isLive(row) shouldBe true
    }

    @Test
    fun `unregister should ignore entities the tracker does not hold`() {
        val row = row(1, 10)
        tracker.spawnIfAbsent(row)
        tracker.unregister(1, entity())
        tracker.isLive(row) shouldBe true
        tracker.unregister(1, spawned.single())
        tracker.isLive(row) shouldBe false
    }

    @Test
    fun `awaitingRows should list loaded rows without a live entity`() {
        val waiting = row(1, 10)
        val spawnedRow = row(2, 10)
        tracker.rowsLoaded(listOf(waiting, spawnedRow))
        tracker.spawnIfAbsent(spawnedRow)

        tracker.awaitingRows() shouldBe listOf(waiting)
        spawned.single().remove()
        tracker.awaitingRows().toSet() shouldBe setOf(waiting, spawnedRow)
    }

    @Test
    fun `rowsLoaded should keep rows added while a chunk was being queried`() {
        val added = row(1, 10)
        tracker.rowsLoaded(listOf(added))
        tracker.rowsLoaded(listOf(row(2, 10)))

        tracker.awaitingRows().map { it.id }.toSet() shouldBe setOf(1L, 2L)
    }

    @Test
    fun `chunkUnloaded should only drop rows in that chunk`() {
        val inChunk = row(1, 10, x = 1.0)
        val elsewhere = row(2, 10, x = 40.0)
        tracker.rowsLoaded(listOf(inChunk, elsewhere))
        tracker.chunkUnloaded(inChunk.pos.chunkKey)

        tracker.awaitingRows() shouldBe listOf(elsewhere)
    }

    @Test
    fun `forget should only drop the row with the matching createdTime`() {
        tracker.rowsLoaded(listOf(row(1, 20)))
        tracker.forget(1, 10)
        tracker.awaitingRows().map { it.id } shouldBe listOf(1L)

        tracker.forget(1, 20)
        tracker.awaitingRows() shouldBe emptyList()
    }

    @Test
    fun `awaitingRows should skip rows being deleted`() = runTest {
        tracker.rowsLoaded(listOf(row(1, 10)))
        tracker.deleting(listOf(1L)) {
            tracker.awaitingRows() shouldBe emptyList()
        }
    }
}
