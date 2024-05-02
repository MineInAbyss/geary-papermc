package com.mineinabyss.geary.papermc.bridge.events.entities

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerShearEntityEvent

@Serializable
@SerialName("geary:on_sheared")
class OnSheared

class EntityShearedBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerShearEntityEvent.emitOnSheared() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.emit<OnSheared>()
    }
}
