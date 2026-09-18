package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Carries a placed prefab's data from the item onto the furniture representing it.
 *
 * Nexo spawns the display before this fires, so geary has already tracked it against an empty
 * container, and its components have to be loaded by hand or the entity stays empty for good.
 *
 * Runs at [EventPriority.LOWEST] so anything else reacting to the placement sees a populated entity.
 */
class NexoFurnitureListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun NexoFurniturePlaceEvent.onPlace() {
        val itemPdc = itemInHand.persistentDataContainer
        if (!itemPdc.hasComponentsEncoded) return

        baseEntity.withGeary {
            val entity = baseEntity.toGearyOrNull() ?: return@withGeary
            entity.loadComponentsFrom(itemPdc)
            entity.encodeComponentsTo(baseEntity)
        }
    }
}
