package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Serializable
@SerialName("geary:on.spawn")
class OnSpawn

class EntitySpawnBridge : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onSpawn() {
        EventHelpers.runSkill<OnSpawn>(gearyEntity)
    }
}
