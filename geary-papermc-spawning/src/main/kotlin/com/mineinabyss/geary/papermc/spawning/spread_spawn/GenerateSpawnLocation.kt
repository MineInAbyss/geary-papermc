package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.spawning.choosing.FindSpotInChunk
import com.mineinabyss.geary.papermc.spawning.choosing.getValidChunk
import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import me.dvyy.sqlite.Database

suspend fun generateSpawnLocation(db: Database, tgs: SpreadSpawner) {
    val randomChunk = getValidChunk(tgs, db) ?: return println("no valid chunk")
    val pos = FindSpotInChunk(randomChunk, tgs)
    if (pos == null) {
        println("Failed to find spawn location")
        return
    }
    println("Found spawn location at $pos")
    db.write {
        tgs.dao.insertSpawnLocation(pos, StoredEntity("mm:abyssal_snail"))
    }
}

