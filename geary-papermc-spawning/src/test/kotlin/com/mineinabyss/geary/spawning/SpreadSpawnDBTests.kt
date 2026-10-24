package com.mineinabyss.geary.spawning

import com.mineinabyss.geary.papermc.data.SpawnsDatabase
import com.mineinabyss.geary.papermc.spawning.backfillRtreeCategories
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.spawning.spread_spawn.StoredEntity
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
import java.util.*

class SpreadSpawnDBTests {
    private val world = mockkClass(World::class) {
        every { uid } returns UUID.fromString("887fe8dd-9a13-46b7-bb46-052150ef27d9")
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
}
