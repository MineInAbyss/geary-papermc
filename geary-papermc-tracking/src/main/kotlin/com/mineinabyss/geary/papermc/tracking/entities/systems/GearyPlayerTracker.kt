package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GearyPlayerTracker(
    world: Geary,
    val gearyMobs: EntityTrackingModule,
) : Listener, Geary by world {
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onPlayerAddToWorld() {
        if (entity !is Player) return
        logger.v { "PlayerJoinEvent: Track ${entity.name}" }
        gearyMobs.bukkit2Geary.getOrCreate(entity)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() {
        logger.v { "PlayerQuitEvent: Untracking ${player.name}" }
        val gearyEntity = player.toGearyOrNull() ?: return
        gearyEntity.encodeComponentsTo(player)
        gearyEntity.removeEntity()
    }
}