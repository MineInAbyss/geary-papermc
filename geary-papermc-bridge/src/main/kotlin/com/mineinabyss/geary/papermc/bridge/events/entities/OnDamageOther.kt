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
@SerialName("geary:on.damage_other")
class OnDamageOther

class EntityDamageOtherBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onDeath() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        EventHelpers.runSkill<OnDamageOther>(gearyEntity)
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onCollision() {
        val target = hitEntity?.toGearyOrNull() ?: return
        val initiator = entity.toGearyOrNull() ?: return
        EventHelpers.runSkill<OnDamageOther>(target, initiator)
    }
}
