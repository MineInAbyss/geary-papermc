package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerShearEntityEvent

@Serializable
@SerialName("geary:on.sheared")
class OnSheared

class ShearedBridge : Listener {
    @EventHandler
    fun PlayerShearEntityEvent.onShear() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        EventHelpers.runSkill<OnSheared>(gearyEntity)
    }
}
