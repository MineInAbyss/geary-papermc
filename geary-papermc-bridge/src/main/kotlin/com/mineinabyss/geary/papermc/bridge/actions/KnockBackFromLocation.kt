package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.cos

/**
 * Applies a force to an entity from a location with the given power, as if from
 * an explosion.
 *
 * Goes on the XZ plane with a given Y angle.
 * Primarily made for attacks which want to knock back players in a minecraft-y way.
 *
 * TODO option to mimic what Propel used to do
 */
@Serializable
@SerialName("geary:knockback")
class KnockBackFromLocation(
    val power: Double,
    val yAngle: Double,
    val scaleWithDistance: Boolean,
    val cancelCurrentVelocity: Boolean
)

fun GearyEntity.knockBack(
    from: Location,
    conf: KnockBackFromLocation,
    entity: BukkitEntity? = get()
): Boolean {
    entity ?: return false
    val targetLoc = entity.location
    if (targetLoc.world != from.world) return false

    val delta = from - entity.location
    delta.y = 0.0

    val velocity = delta.toVector().normalize()
    velocity.y = cos(conf.yAngle)  // Not sure about this, should probably get all of the
    // relevant angles and set the velocity based on those
    val distance = from.distance(entity.location)

    // TODO This is wacky placeholder math, probably change it at some point
    val maxForce = (conf.power * 5.0)
    var scalar: Double
    if (conf.scaleWithDistance) {
        if (distance == 0.0) {
            scalar = maxForce
        } else {
            scalar = conf.power / (distance * distance * distance)
            if (scalar > maxForce)
                scalar = maxForce
        }
    } else
        scalar = conf.power

    velocity.multiply(scalar)

    if (conf.cancelCurrentVelocity)
        entity.velocity = velocity
    else
        entity.velocity.add(velocity)

    return true
}
