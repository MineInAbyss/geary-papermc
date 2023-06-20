package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.papermc.bridge.components.Attacked
import com.mineinabyss.geary.papermc.bridge.components.Landed
import com.mineinabyss.geary.papermc.bridge.components.Touched
import com.mineinabyss.geary.papermc.bridge.helpers.setBukkitEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.*
import org.bukkit.event.entity.ProjectileHitEvent

class MobActionsBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onCollision() {
        hitEntity?.toGeary()?.callEvent(source = entity.toGeary()) {
            //TODO check direction
            if ((hitEntity as? Player)?.isBlocking != true)
                add<Touched>()
            setBukkitEvent(this@onCollision)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.onProjectileLand() {
        entity.toGeary().callEvent {
            add<Landed>()
            setBukkitEvent(this@onProjectileLand)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onDamage() {
        entity.toGeary().callEvent(source = damager.toGeary()) {
            if (this@onDamage.cause in setOf(CONTACT, ENTITY_ATTACK, ENTITY_SWEEP_ATTACK)) {
                add<Touched>()
                add<Attacked>()
            }
            setBukkitEvent(this@onDamage)
        }
    }
}
