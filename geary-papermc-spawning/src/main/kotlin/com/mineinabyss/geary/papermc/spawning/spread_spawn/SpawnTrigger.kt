package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import org.bukkit.Chunk
import kotlin.random.Random

suspend fun triggerSpawn(chunk: Chunk, db: Database, dao: SpawnLocationsDAO) {
    val world = chunk.world
    val chunkEntities = chunk.entities.toList()
    val list: List<SpreadSpawnLocation> =
        db.read { dao.getSpawnsInChunk(chunk) }
    for (spread: SpreadSpawnLocation in list) {
        val idToCheck = spread.id
        val alreadyExists = chunkEntities.any { entity ->
            val checkspread = entity.toGearyOrNull()?.get<SpreadSpawnLocation>() ?: return@any false
            checkspread.id == idToCheck
        }
        if (alreadyExists) {
            continue
        }

        val loc = spread.location.toLocation(world)
        loc.yaw = Random.nextFloat() * 360f
        val type = spread.stored.asSpawnType() ?: continue
        val bukkitEntity = type.spawnAt(loc)
        val gearyEntity = bukkitEntity.toGearyOrNull()
        gearyEntity?.set<SpreadSpawnLocation>(spread)
    }
}