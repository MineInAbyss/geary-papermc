package com.mineinabyss.geary.papermc.spawning.database.schema

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import me.dvyy.sqlite.Database
import org.bukkit.Location

class SpawningSchema(
    val db: Database,
) {
    val tables = listOf(SpawnLocationsTable, SpawnLocationsRtree, SpawnLocations)

    suspend fun init() = db.write {
        tables.forEach { it.create() }
    }
}

suspend fun main() {
    val db = Database(BundledSQLiteDriver(), path = "test.db", defaultIdentity = 0)
    SpawningSchema(db).init()
    val locs = SpawnLocationsDAO()
    db.write {
        locs.insertSpawnLocation("test", Location(null, 1000.0, 0.0, 0.0))
        locs.getSpawnsNear(Location(null, 0.0, 0.0, 0.0), 10.0)
    }.let { println(it) }
}
