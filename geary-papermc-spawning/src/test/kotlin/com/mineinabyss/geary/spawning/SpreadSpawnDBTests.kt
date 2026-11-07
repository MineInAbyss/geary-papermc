package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.data.SpawnsDatabase
import com.mineinabyss.geary.papermc.spawning.backfillRtreeCategories
import com.mineinabyss.geary.papermc.spawning.dropUnkeyedSurfaceChunks
import com.mineinabyss.geary.papermc.spawning.spread_spawn.ChunkSurface
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.StoredEntity
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfacePos
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SurfaceScanner
import com.mineinabyss.geary.papermc.spawning.spread_spawn.chunkKey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.test.runTest
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.days

class SpreadSpawnDBTests {
    private val world = mockkClass(World::class) {
        every { uid } returns UUID.fromString("887fe8dd-9a13-46b7-bb46-052150ef27d9")
    }
    private val otherWorld = mockkClass(World::class) {
        every { uid } returns UUID.fromString("0b8a1f5e-3c43-4d55-9a3e-6f1b2c7d8e90")
    }
    private val schema = SpawnsDatabase()
    private val box = BoundingBox(-100.0, -100.0, -100.0, 100.0, 100.0, 100.0)

    private fun repository() = SpreadSpawnRepository(Database.temporary { schema.create() }, schema.spawnQueries)

    @Test
    fun `typed queries should only match spawns of that category`() = runTest {
        val spawns = repository()
        repeat(3) { spawns.insertSpawnLocation(Location(world, it.toDouble(), 0.0, 0.0), StoredEntity("mob", "praying_skeleton")) }
        spawns.insertSpawnLocation(Location(world, 10.0, 0.0, 0.0), StoredEntity("mob", "charred_skeleton"))

        spawns.countSpawnsInBB(world, box, listOf("praying_skeleton")) shouldBe 3
        spawns.countSpawnsInBB(world, box, listOf("charred_skeleton")) shouldBe 1
        spawns.countSpawnsInBB(world, box, listOf("praying_skeleton", "charred_skeleton")) shouldBe 4
        spawns.countSpawnsInBB(world, box, listOf("unknown")) shouldBe 0
        spawns.countNearby(Location(world, 10.0, 0.0, 0.0), 5.0, listOf("charred_skeleton")) shouldBe 1

        val closest = spawns.getClosestSpawn(Location(world, 9.0, 0.0, 0.0), 100.0, listOf("praying_skeleton")).shouldNotBeNull()
        closest.location.x shouldBe 2.0
        spawns.getClosestSpawn(Location(world, 0.0, 0.0, 0.0), 100.0, listOf("unknown")).shouldBeNull()
    }

    @Test
    fun `backfill should assign categories to rows written without one`() = runTest {
        val db = Database.temporary { schema.create() }
        val spawns = SpreadSpawnRepository(db, schema.spawnQueries)
        db.write {
            exec("INSERT INTO spawn_categories(name) VALUES ('praying_skeleton')")
            exec("INSERT INTO spawn_rtree(minX, maxX, minY, maxY, minZ, maxZ) VALUES (0, 0, 0, 0, 0, 0)")
            exec("""INSERT INTO spawn_data(id, data) SELECT last_insert_rowid(), json('{"type":"mob","category":"praying_skeleton","createdTime":0}')""")
        }
        spawns.countSpawnsInBB(world, box, listOf("praying_skeleton")) shouldBe 0

        db.write { backfillRtreeCategories() }

        spawns.countSpawnsInBB(world, box, listOf("praying_skeleton")) shouldBe 1
    }

    @Test
    fun `getSpawnsInBB should filter by category`() = runTest {
        val spawns = repository()
        spawns.insertSpawnLocation(Location(world, 1.0, 2.0, 3.0), StoredEntity("mob", "praying_skeleton"))
        spawns.insertSpawnLocation(Location(world, 4.0, 5.0, 6.0), StoredEntity("mob", "charred_skeleton"))
        spawns.insertSpawnLocation(Location(world, 500.0, 0.0, 0.0), StoredEntity("mob", "praying_skeleton"))

        spawns.getSpawnsInBB(world, box, listOf("praying_skeleton")).map { it.location.x } shouldBe listOf(1.0)
        spawns.getSpawnsInBB(world, box).size shouldBe 2
    }

    @Test
    fun `deleteSpawnLocations should only delete the listed ids`() = runTest {
        val spawns = repository()
        val ids = List(3) { spawns.insertSpawnLocation(Location(world, it.toDouble(), 0.0, 0.0), StoredEntity("mob", "praying_skeleton")).id }

        spawns.deleteSpawnLocations(world, ids.take(2))

        spawns.getSpawnsInBB(world, box).map { it.id } shouldBe listOf(ids[2])
    }

    @Test
    fun `deleting old spawns should keep listed and recent ids`() = runTest {
        val spawns = repository()
        val old = StoredEntity("mob", "praying_skeleton", createdTime = Instant.ofEpochSecond(0))
        val kept = spawns.insertSpawnLocation(Location(world, 0.0, 0.0, 0.0), old).id
        spawns.insertSpawnLocation(Location(world, 1.0, 0.0, 0.0), old)
        val recent = spawns.insertSpawnLocation(Location(world, 2.0, 0.0, 0.0), StoredEntity("mob", "praying_skeleton")).id

        spawns.deleteSpawnsOlderThan(world, 1.days, listOf(kept))

        spawns.getSpawnsInBB(world, box).map { it.id }.toSet() shouldBe setOf(kept, recent)
    }

    @Test
    fun `surface chunks should round trip, replace and drop`() = runTest {
        val spawns = repository()
        val surface = ChunkSurface(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8), intArrayOf(SurfacePos.pack(1, 2, 300, 5)), SurfaceScanner.VERSION, 1234)
        val key = chunkKey(-5, 7)
        suspend fun load() = buildMap { spawns.loadSurfaces(world) { k, s -> put(k, s) } }

        spawns.putSurfaces(world, mapOf(key to surface))
        spawns.putSurfaces(world, mapOf(key to surface.withoutPosition(1, 2, 300)))
        val loaded = load().getValue(key)
        load().size shouldBe 1
        loaded.histogram.toList() shouldBe surface.histogram.toList()
        loaded.positions.toList() shouldBe emptyList()
        loaded.scannedAt shouldBe 1234

        spawns.dropSurfaces(world)
        load().size shouldBe 0
    }

    @Test
    fun `surface chunks should be scoped to their world`() = runTest {
        val spawns = repository()
        val key = chunkKey(3, 4)
        val surface = ChunkSurface(IntArray(8), IntArray(0), SurfaceScanner.VERSION, 1)
        suspend fun keysIn(world: World) = buildList { spawns.loadSurfaces(world) { k, _ -> add(k) } }

        spawns.putSurfaces(world, mapOf(key to surface))
        spawns.putSurfaces(otherWorld, mapOf(key to surface, chunkKey(9, 9) to surface))
        keysIn(world) shouldBe listOf(key)
        keysIn(otherWorld).size shouldBe 2

        spawns.dropSurfaces(otherWorld)
        keysIn(world) shouldBe listOf(key)
        keysIn(otherWorld).shouldBeEmpty()
    }

    @Test
    fun `surface chunks without a world column should be dropped and recreated`() = runTest {
        val db = Database.temporary {
            exec("CREATE TABLE surface_chunks (chunk INTEGER PRIMARY KEY, version INTEGER NOT NULL, scanned_at INTEGER NOT NULL, histogram BLOB NOT NULL, positions BLOB NOT NULL) STRICT")
            exec("INSERT INTO surface_chunks VALUES (1, 1, 1, x'00', x'00')")
            dropUnkeyedSurfaceChunks()
            schema.create()
        }
        val spawns = SpreadSpawnRepository(db, schema.spawnQueries)
        val surface = ChunkSurface(IntArray(8), IntArray(0), SurfaceScanner.VERSION, 1)

        spawns.putSurfaces(world, mapOf(chunkKey(0, 0) to surface))
        buildList { spawns.loadSurfaces(world) { k, _ -> add(k) } } shouldBe listOf(chunkKey(0, 0))
    }

    @Test
    fun `keyed surface chunks should survive the migration`() = runTest {
        val db = Database.temporary { schema.create() }
        val spawns = SpreadSpawnRepository(db, schema.spawnQueries)
        spawns.putSurfaces(world, mapOf(chunkKey(0, 0) to ChunkSurface(IntArray(8), IntArray(0), SurfaceScanner.VERSION, 1)))

        db.write { dropUnkeyedSurfaceChunks() }

        buildList { spawns.loadSurfaces(world) { k, _ -> add(k) } } shouldBe listOf(chunkKey(0, 0))
    }

    @Test
    fun `getSpawnsWithIds should return matching rows with their created time`() = runTest {
        val spawns = repository()
        val created = Instant.ofEpochSecond(42)
        val id = spawns.insertSpawnLocation(Location(world, 0.0, 0.0, 0.0), StoredEntity("mob", "praying_skeleton", created)).id
        spawns.insertSpawnLocation(Location(world, 1.0, 0.0, 0.0), StoredEntity("mob", "praying_skeleton"))

        spawns.getSpawnsWithIds(world, listOf(id)).single().createdTime shouldBe 42
        spawns.getSpawnsWithIds(world, emptyList()).shouldBeEmpty()
    }
}
