package com.mineinabyss.geary.papermc.spawning.targeted

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.triggerSpawn
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import org.bukkit.Chunk
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.plugin.Plugin
import kotlin.random.Random
import org.bukkit.entity.Entity
import kotlin.text.get
import kotlin.text.set
import kotlin.times

// listen to chunk load event and spawn entity if its in the list of entities
class ListSpawnListener(
    private val spawner: TargetedSpawner,
    val db: Database,
    val plugin: Plugin
) : Listener {

    /*TODO: sometimes the mobs wont spawn properly because the chunk will already be loaded in one way or another
     * this isn't really an issue since 1: its super rare and 2: no one will notice
     * UNLESS the player has a ghost seek, in which case, they'll follow a ghost trail
     * to fix this, we could add a trigger on the ghost seek to force spawn the entities if it isn't already spawned
     * if the player with a ghost seek gets close enough (so like 4 pings or something similar)*/
    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        plugin.launch {
            triggerSpawn(chunk, db, spawner.dao)
        }
    }
}
