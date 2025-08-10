package com.mineinabyss.geary.papermc.spawning.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.database.dao.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import me.dvyy.sqlite.Database
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.Plugin

class SpreadEntityDeathListener(
    private val spawner: SpreadSpawner,
    val db: Database,
    val plugin: Plugin,
    val mainWorld: World,
) : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity.toGearyOrNull() ?: return
        val spread = entity.get<SpreadSpawnLocation>() ?: return
        val id = spread.id
        val dao = SpawnLocationsDAO()

        plugin.launch {
            db.write {
                dao.deleteSpawnLocation(mainWorld, id)
            }
        }
    }

}