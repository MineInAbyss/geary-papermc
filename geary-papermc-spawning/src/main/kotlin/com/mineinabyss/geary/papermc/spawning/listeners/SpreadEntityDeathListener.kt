package com.mineinabyss.geary.papermc.spawning.listeners

import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadEntityTracker
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class SpreadEntityDeathListener(
    private val spawns: SpreadSpawnRepository,
    private val tracker: SpreadEntityTracker,
    private val mainWorld: World,
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onEntityDeath(event: EntityDeathEvent) {
        val (id, createdTime) = tracker.tagOf(event.entity) ?: return
        tracker.unregister(id, event.entity)
        tracker.forget(id, createdTime)
        tracker.deleting(listOf(id)) {
            val row = spawns.getSpawnsWithIds(mainWorld, listOf(id)).firstOrNull()
            // A different createdTime means the id was reused by a newer row, which this entity does not own
            if (row != null && row.createdTime == createdTime) spawns.deleteSpawnLocation(mainWorld, id)
        }
    }
}
