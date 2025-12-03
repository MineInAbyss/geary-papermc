package com.mineinabyss.geary.papermc.spawning.listeners

import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class SpreadEntityDeathListener(
    private val spawns: SpreadSpawnRepository,
    val mainWorld: World,
) : Listener {

    @EventHandler
    suspend fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity.toGearyOrNull() ?: return
        val spread = entity.get<SpreadSpawnLocation>() ?: return
        val id = spread.id
        spawns.deleteSpawnLocation(mainWorld, id)
    }
}