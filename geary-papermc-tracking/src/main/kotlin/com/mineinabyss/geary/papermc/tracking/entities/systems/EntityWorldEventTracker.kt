package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesUnloadEvent

class EntityWorldEventTracker(
    world: Geary,
    val gearyMobs: EntityTrackingModule,
) : Listener, Geary by world {
    /** Add entities to ECS when they are added to Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityAddToWorldEvent.onBukkitEntityAdd() {
        if (entity is Player) return // Separate listener for players
        logger.v { "EntityAddToWorldEvent: Tracking bukkit entity ${entity.toGearyOrNull()?.id} (${entity.type} ${entity.uniqueId})" }
        gearyMobs.bukkit2Geary.getOrCreate(entity)
    }

    /** Remove entities from ECS when they are removed from Bukkit for any reason (Uses PaperMC event) */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntityRemoveFromWorldEvent.onBukkitEntityRemove() {
        // Only remove player from ECS on disconnect, not death
        if (entity is Player) return

        // We remove the geary entity a bit later because paper has a bug where stored entities call load/unload/load
        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            if (entity.isValid) return@scheduleSyncDelayedTask // If the entity is still valid, it's the paper bug
            logger.v { "EntityRemoveFromWorldEvent: Calling removeEntity for ${entity.toGearyOrNull()?.id} (${entity.type} ${entity.uniqueId})" }
            entity.toGearyOrNull()?.let {
                gearyMobs.bukkit2Geary.fireRemoveFromWorldEvent(entity, it)
                it.removeEntity()
            }
        }, 10)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun EntitiesUnloadEvent.onEntitiesUnload() {
        if (entities.isEmpty()) return
        logger.v { "EntitiesUnloadEvent: Saving ${entities.size} entities in chunk..." }

        entities.forEach {
            val gearyEntity = it.toGearyOrNull() ?: return@forEach
            gearyEntity.encodeComponentsTo(it)
        }
    }
}
