package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.serialization.PotionEffectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.ShulkerBullet
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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
        if (alreadyExists(player, radius))
            return
        val targetedEntity = getTarget(player, radius) ?: return
        repeat(amount) {
            val bullet = player.world.spawn(player.eyeLocation.clone().add(player.eyeLocation.direction.clone().multiply(1)), ShulkerBullet::class.java)
            bullet.target = targetedEntity
            val gearyEntity = bullet.toGearyOrNull() ?: return
            gearyEntity.set(
                ShulkerBulletData(amount, damage, speed, duration, player, effects)
            )
        }
    }
}

fun alreadyExists(player: Player, radius: Double): Boolean {
    val entities = player.world.getNearbyLivingEntities(player.location, radius) { entity ->
        entity is ShulkerBullet && entity.toGearyOrNull()?.get<ShulkerBulletData>()?.owner == player
    }
    return entities.isNotEmpty()
}

fun getTarget(player: Player, radius: Double): Entity? {
    val direction = player.eyeLocation.direction
    val startLocation = player.eyeLocation.clone().add(direction.clone().multiply(1))
    val result = player.world.rayTraceEntities(startLocation, direction, 50.0)
    return result?.hitEntity ?: getClosestEntity(player, radius)
}

fun getClosestEntity(player: Player, radius: Double): Entity? {
    val predicate = { entity: Entity -> entity != player }
    val entities = player.world.getNearbyLivingEntities(player.location, radius, predicate)
    return entities.minByOrNull { it.location.distance(player.location) }
}

class ShulkerBulletHitListener : Listener {
    @EventHandler
    fun ProjectileHitEvent.onProjectileHit() {
        val data = (entity as? ShulkerBullet)?.toGearyOrNull()?.get<ShulkerBulletData>() ?: return
        val hitEntity = hitEntity as? LivingEntity ?: return
        this.isCancelled = true;

        hitEntity.damage(data.damage, data.owner)
        hitEntity.removePotionEffect(PotionEffectType.LEVITATION)
        data.effects.forEach(hitEntity::addPotionEffect)
        entity.remove()
    }
}
