package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.Database

class SpawningSchema(
    val db: Database,
) {
    val tables = listOf(SpawnLocationsTable, SpawnLocationsRtree, SpawnLocations)

    suspend fun init() = db.write {
        tables.forEach { it.create() }
    }
}
