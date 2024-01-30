package com.mineinabyss.geary.papermc.tracking.entities.systems.removevanillamobs

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RemoveVanillaMobsListener : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onAddVanillaMob() {
        val removeTypes = gearyPaper.config.removeVanillaMobTypes
        if (removeTypes.isEmpty()) return
        if (entity.type !in removeTypes) return
        if (gearyEntity.prefabs.isNotEmpty()) return // Vanilla mobs currently don't have prefabs TODO ensure this works if we ever add vanilla prefabs

        UpdateMob.scheduleRemove(entity)
    }
}
