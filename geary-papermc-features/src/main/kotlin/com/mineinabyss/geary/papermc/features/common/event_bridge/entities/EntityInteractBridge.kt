package com.mineinabyss.geary.papermc.features.common.event_bridge.entities

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent

@Serializable
@SerialName("geary:on_damaged")
sealed class OnDamaged : EventBridge

@Serializable
@SerialName("geary:on_damage_other")
sealed class OnDamageOther : EventBridge

@Serializable
@SerialName("geary:on_interact")
sealed class OnInteract : EventBridge

class EntityDamageBridge : Listener {
    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.emitEntityDamaged() {
        val gearyEntity = entity.toGearyOrNull()
        val damager = damager.toGearyOrNull()
        gearyEntity?.emit<OnDamaged>()
        damager?.emit<OnDamageOther>()
    }

    @EventHandler(ignoreCancelled = true)
    fun ProjectileHitEvent.emitProjectileDamaged() {
        val projectile = entity.toGearyOrNull()
        val gearyEntity = hitEntity?.toGearyOrNull()
        gearyEntity?.emit<OnDamaged>()
        projectile?.emit<OnDamageOther>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractAtEntityEvent.emitOnInteract() {
        val gearyEntity = rightClicked.toGearyOrNull()
        gearyEntity?.emit<OnInteract>()

        //TODO update event system so we can run conditions on the player here, not just target
        if (player.inventory.itemInMainHand.type == Material.SHEARS) {
            gearyEntity?.emit<OnSheared>()
        }
    }
}
