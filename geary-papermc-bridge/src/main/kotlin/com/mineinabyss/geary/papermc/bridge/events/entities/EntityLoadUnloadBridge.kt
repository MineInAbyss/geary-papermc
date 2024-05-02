package com.mineinabyss.geary.papermc.bridge.events.entities

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
sealed class OnSpawn

@Serializable
@SerialName("geary:on_load")
sealed class OnLoad

@Serializable
@SerialName("geary:on_death")
class OnDeath

class EntitySpawnBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun GearyEntityAddToWorldEvent.emitOnLoad() {
        gearyEntity.emit<OnLoad>()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntitySpawnEvent.emitOnSpawn() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.emit<OnSpawn>()
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDeathEvent.emitOnDeath() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.emit<OnDeath>()
    }
}
