package com.mineinabyss.geary.spawning

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawningSchema
import me.dvyy.sqlite.Database
import org.bukkit.Location

//TODO write an actual test
suspend fun main() {
    val db = Database(BundledSQLiteDriver(), path = "test.db", defaultIdentity = 0)
    SpawningSchema(db).init()
    val locs = SpawnLocationsDAO()
    db.write {
        locs.insertSpawnLocation(Location(null, 1000.0, 0.0, 0.0), StoredEntity("hello"))
        locs.getSpawnsNear(Location(null, 1000.0, 0.0, 0.0), 10.0)
    }.let { println(it) }
}
