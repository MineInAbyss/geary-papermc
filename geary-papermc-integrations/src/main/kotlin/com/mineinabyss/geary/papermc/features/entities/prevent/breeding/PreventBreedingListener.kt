package com.mineinabyss.geary.papermc.features.entities.prevent.breeding

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityEnterLoveModeEvent

class PreventBreedingListener : Listener {
    @EventHandler
    fun EntityEnterLoveModeEvent.cancelLove() {
        if (entity.toGearyOrNull()?.has<PreventBreeding>() == true) isCancelled = true
    }

    @EventHandler
    fun EntityBreedEvent.cancelBreed() {
        if (entity.toGearyOrNull()?.has<PreventBreeding>() == true) isCancelled = true
    }
}
