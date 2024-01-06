package com.mineinabyss.geary.papermc.bridge.events.entities

import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent

@Serializable
@SerialName("geary:on.damaged")
class OnDamaged

class EntityDamagedBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onDeath() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        EventHelpers.runSkill<OnDamaged>(gearyEntity)
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onCollision() {
        val gearyEntity = hitEntity?.toGearyOrNull() ?: return
        EventHelpers.runSkill<OnDamaged>(gearyEntity)
    }
}
