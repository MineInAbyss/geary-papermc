package com.mineinabyss.geary.papermc.features.common.event_bridge.entities

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent

@Serializable
@SerialName("geary:on_spawn")
sealed class OnSpawn : EventBridge

@Serializable
@SerialName("geary:on_load")
sealed class OnLoad : EventBridge

@Serializable
@SerialName("geary:on_death")
class OnDeath : EventBridge

class EntityLoadUnloadBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun GearyEntityAddToWorldEvent.emitOnLoad() {
        gearyEntity.emit<OnLoad>()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntitySpawnEvent.emitOnSpawn() = with(entity.world.toGeary()) {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.emit<OnSpawn>()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDeathEvent.emitOnDeath() = with(entity.world.toGeary()) {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.emit<OnDeath>()
    }
}
