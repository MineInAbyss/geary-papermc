package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.withGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GearyPlayerTracker : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onPlayerAddToWorld() {
        if (entity !is Player) return
        entity.withGeary {
            logger.v { "PlayerJoinEvent: Track ${entity.name}" }
            getAddon(EntityTracking).bukkit2Geary.getOrCreate(entity)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() = player.withGeary {
        logger.v { "PlayerQuitEvent: Untracking ${player.name}" }
        val gearyEntity = player.toGearyOrNull() ?: return
        gearyEntity.encodeComponentsTo(player)
        gearyEntity.removeEntity()
    }
}