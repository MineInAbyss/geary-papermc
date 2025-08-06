package com.mineinabyss.geary.papermc.spawning.targeted

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.helpers.launchWithTicket
import com.mineinabyss.geary.papermc.sqlite.blockingRead
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.plugin.Plugin
import kotlin.random.Random
import org.bukkit.entity.Entity

// listen to chunk load event and spawn entity if its in the list of entities
class ListSpawnListener(
    private val spawner: TargetedSpawner,
    val db: Database,
    val plugin: Plugin
) : Listener {





    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        val world = world
        val chunkEntities: List<Entity> = chunk.entities.toList()
       // plugin.launch {
            val list: List<SpreadSpawnLocation> =
                db.blockingRead { spawner.dao.getSpawnsInChunk(chunk) }
           for (spread: SpreadSpawnLocation in list) {
//                val idToCheck = spread.id
//                val alreadyExists = chunkEntities.any { entity ->
//                    val checkspread =  entity.toGearyOrNull()?.get<SpreadSpawnLocation>() ?: return@any false
//                    checkspread.id == idToCheck
//                }
//                if (alreadyExists)  {
//                    println("double spawn avoided")
//                    break
//                }

                val loc = spread.location.toLocation(world)
                loc.yaw = Random.nextFloat() * 360f
                val type = spread.stored.asSpawnType() ?: break
                val bukkitEntity = type.spawnAt(loc)
                val gearyEntity = bukkitEntity.toGearyOrNull()
                gearyEntity?.set<SpreadSpawnLocation>(spread)
            }
        }
   // }
}