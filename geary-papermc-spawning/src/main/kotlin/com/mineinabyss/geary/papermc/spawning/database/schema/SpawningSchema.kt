package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.Database
import org.bukkit.World

class SpawningSchema(
    val db: Database,
    val supportedWorlds: List<World>
) {
    suspend fun init() = db.write {
        supportedWorlds.forEach { world ->
            SpawnLocationTables.dataTable(world).create()
            SpawnLocationTables.rtree(world).create()
            SpawnLocationTables.locationsView(world).create()
        }
    }
}
