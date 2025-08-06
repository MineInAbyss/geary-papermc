package com.mineinabyss.geary.papermc.spawning.targeted

import com.mineinabyss.geary.papermc.spawning.database.dao.StoredEntity
import me.dvyy.sqlite.Database

suspend fun generateSpawnLocation(db: Database, tgs: TargetedSpawner) {
    val randomChunk = getValidChunk(tgs,db) ?: return println("no valid chunk")
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

