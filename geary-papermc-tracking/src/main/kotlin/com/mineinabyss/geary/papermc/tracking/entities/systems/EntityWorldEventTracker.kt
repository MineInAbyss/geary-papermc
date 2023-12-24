package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EntityWorldEventTracker : Listener {
    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        gearyMobs.bukkit2Geary.getOrCreate(entity)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        // We remove the geary entity one tick after the Bukkit one has been removed to ensure nothing
        // else that tries to access the geary entity from Bukkit will create a new entity.
        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            if(entity.isValid) return@scheduleSyncDelayedTask
            entity.toGearyOrNull()?.removeEntity()
        }, 10)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onPlayerLogin() {
        gearyMobs.bukkit2Geary.getOrCreate(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() {
        player.toGearyOrNull()?.removeEntity()
    }
}
