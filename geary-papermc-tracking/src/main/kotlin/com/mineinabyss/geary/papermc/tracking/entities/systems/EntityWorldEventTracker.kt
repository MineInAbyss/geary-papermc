package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
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
import org.bukkit.event.world.EntitiesUnloadEvent

class EntityWorldEventTracker : Listener {
    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return
        geary.logger.d("EntityAddToWorldEvent: Track bukkit entity ${entity.uniqueId} (UUID: ${entity.uniqueId})")
        gearyMobs.bukkit2Geary.getOrCreate(entity)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return

        geary.logger.d("EntityRemoveFromWorldEvent: Schedule untrack bukkit entity ${entity.uniqueId} (UUID: ${entity.uniqueId})")

        // We remove the geary entity a bit later because paper has a bug where stored entities call load/unload/load
        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            if (entity.isValid) return@scheduleSyncDelayedTask // If the entity is still valid, it's the paper bug
            geary.logger.d("EntityRemoveFromWorldEvent: Call removeEntity ${entity.uniqueId} (UUID: ${entity.uniqueId})")
            entity.toGearyOrNull()?.removeEntity()
        }, 10)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntitiesUnloadEvent.onEntitiesUnload() {
        geary.logger.d("EntitiesUnloadEvent: Untrack ${entities.size} entities")
        entities.forEach {
            val gearyEntity = it.toGearyOrNull() ?: return@forEach
            geary.logger.d("EntitiesUnloadEvent: Untrack bukkit entity ${it.uniqueId} (UUID: ${it.uniqueId})")
            gearyEntity.encodeComponentsTo(it)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerJoinEvent.onPlayerLogin() {
        geary.logger.d("PlayerJoinEvent: Track ${player.name}")
        gearyMobs.bukkit2Geary.getOrCreate(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerQuitEvent.onPlayerLogout() {
        geary.logger.d("PlayerJoinEvent: Untrack ${player.name}")
        val gearyEntity = player.toGearyOrNull() ?: return
        gearyEntity.encodeComponentsTo(player)
        gearyEntity.removeEntity()
    }
}
