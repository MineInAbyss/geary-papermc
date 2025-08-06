package com.mineinabyss.geary.papermc.spawning.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import org.bukkit.Chunk
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.plugin.Plugin
import kotlin.random.Random

// listen to chunk load event and spawn entity if its in the list of entities
class ListSpawnListener(
    private val dao: SpawnLocationsDAO,
    private val db: Database,
    private val plugin: Plugin
) : Listener {

    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        plugin.launch {
            triggerSpawn(chunk)
        }
    }

    suspend fun triggerSpawn(chunk: Chunk) {
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
}
