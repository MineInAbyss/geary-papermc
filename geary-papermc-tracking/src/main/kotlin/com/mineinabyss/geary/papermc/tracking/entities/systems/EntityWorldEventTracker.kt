package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
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
        entity.toGeary()
    }
    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        // We remove the geary entity one tick after the Bukkit one has been removed to ensure nothing
        // else that tries to access the geary entity from Bukkit will create a new entity.
        entity.toGearyOrNull()?.removeEntity()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onPlayerLogin() {
        player.toGeary()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() {
        player.toGearyOrNull()?.removeEntity()
    }
}
