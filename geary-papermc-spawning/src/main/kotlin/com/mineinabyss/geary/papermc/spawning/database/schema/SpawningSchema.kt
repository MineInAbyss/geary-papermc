package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.WriteTransaction
import org.bukkit.World

class SpawningSchema(val supportedWorlds: List<World>) {
    context(tx: WriteTransaction)
    fun init() {
        supportedWorlds.forEach { world ->
            SpawnLocationTables.dataTable(world).create()
            SpawnLocationTables.rtree(world).create()
            SpawnLocationTables.locationsView(world).create()
        }
    }
}
