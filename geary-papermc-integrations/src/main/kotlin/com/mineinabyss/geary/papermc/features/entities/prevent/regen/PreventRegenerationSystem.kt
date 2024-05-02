package com.mineinabyss.geary.papermc.features.entities.prevent.regen

import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent

class PreventRegenerationSystem : Listener {
    @EventHandler
    fun EntityRegainHealthEvent.onEntityHeal() {
        entity.toGearyOrNull()?.with { prevent: PreventRegeneration ->
            if (prevent.reason.isEmpty() || regainReason in prevent.reason) isCancelled = true
        }
    }
}
