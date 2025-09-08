package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.ShulkerBullet
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

data class ShulkerBulletData(
    val amount: Int,
    val damage: Double,
    val speed: Int,
    val duration: Int,
    val owner: Player,
    val effects: List<PotionEffect>,
)

@Serializable
@SerialName("geary:shulker_bullet")
class SpawnShulkerBulletAction(
    val amount: Int,
    val damage: Double,
    val radius: Double,
    val homing: Boolean = true,
    val speed: Int,
    val duration: Int,
    val effects: List<@Serializable(with = PotionEffectSerializer::class) PotionEffect>,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return
        if (alreadyExists(player, radius)) {
            return
        }
        val targetedEntity = getTarget(player, radius) ?: run {
            player.sendMessage("no target")
            return
        }
        repeat(amount) {
            val bullet = player.world.spawnEntity(
                player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(1)),
                EntityType.SHULKER_BULLET
            ) as ShulkerBullet
            bullet.target = targetedEntity
            val gearyEntity = bullet.toGearyOrNull() ?: return
            gearyEntity.set(
                ShulkerBulletData(
                    amount = amount,
                    damage = damage,
                    speed = speed,
                    duration = duration,
                    owner = player,
                    effects = effects
                )
            )
        }
    }
}

fun alreadyExists(player: Player, radius: Double): Boolean {
    val entities = player.world.getNearbyEntities(player.location, radius, radius, radius)
        .filter { it is ShulkerBullet && it.toGearyOrNull()?.get<ShulkerBulletData>()?.owner == player }
    return entities.isNotEmpty()
}

fun getTarget(player: Player, radius: Double): Entity? {
    val direction: Vector = player.eyeLocation.direction
    val startLocation = player.eyeLocation.clone().add(direction.clone().multiply(1))
    val result = player.world.rayTraceEntities(startLocation, direction, 50.0)
    val targetLocation = result?.hitPosition?.toLocation(player.world)
    var target = result?.hitEntity
    if (targetLocation == null) {
        target = getClosestEntity(player, radius)
    }
    return target
}

fun getClosestEntity(player: Player, radius: Double): Entity? {
    val entities = player.world.getNearbyEntities(player.location, radius, radius, radius)
        .filter { it != player }
    return entities.minByOrNull { it.location.distance(player.location) }
}

class ShulkerBulletHitListener : Listener {
    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val bullet = event.entity as? ShulkerBullet ?: return
        val gearyEntity = bullet.toGearyOrNull() ?: return
        val data = gearyEntity.get<ShulkerBulletData>() ?: return

        val hit = event.hitEntity ?: return
        if (hit is org.bukkit.entity.LivingEntity) {
            hit.damage(data.damage) // could be customized
            hit.removePotionEffect(PotionEffectType.LEVITATION)
            data.effects.forEach { effect ->
                hit.addPotionEffect(effect)
            }
        }
    }
}