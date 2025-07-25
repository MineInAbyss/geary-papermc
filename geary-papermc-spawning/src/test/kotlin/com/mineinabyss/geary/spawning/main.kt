package com.mineinabyss.geary.spawning

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawningSchema
import io.mockk.every
import io.mockk.mockkClass
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

//TODO write an actual test
suspend fun main() {
    val world = mockkClass(World::class) {
        every { uid } returns UUID.fromString("887fe8dd-9a13-46b7-bb46-052150ef27d9")
    }

    val db = Database(BundledSQLiteDriver(), path = "test.db", defaultIdentity = 0)
    SpawningSchema(db, listOf(world)).init()
    val locs = SpawnLocationsDAO()
    db.write {
        locs.insertSpawnLocation(Location(world, 1000.0, 0.0, 0.0), StoredEntity("hello"))
        locs.deleteSpawnsOlderThan(world, 10.seconds)
        locs.getSpawnsNear(Location(world, 1000.0, 0.0, 0.0), 10.0)
    }.let { println(it) }
}
