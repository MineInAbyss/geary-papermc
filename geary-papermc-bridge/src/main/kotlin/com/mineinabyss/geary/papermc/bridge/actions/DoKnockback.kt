package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import com.mineinabyss.idofront.operators.times
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.Contextual
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
class DoKnockback(
    val power: Double = 1.0,
    val yAngle: Double,
    val scaleWithDistance: Boolean = true,
    val cancelCurrentVelocity: Boolean = true,
    val center: Input<@Contextual Location>
)

class DoKnockbackSystem : GearyListener() {
    val Pointers.entity by get<BukkitEntity>().on(target)
    val Pointers.doKnockback by get<DoKnockback>().on(source)

    override fun Pointers.handle() {
        val targetLoc = entity.location
        val center = doKnockback.center.get(this)
        if (targetLoc.world != center.world) return

        val delta = center - entity.location
        delta.y = 0.0

        val velocity = delta.toVector().multiply(-1).normalize().apply {
            y = cos(Math.toRadians(doKnockback.yAngle))
        } * doKnockback.power

          // Not sure about this, should probably get all of the

        if (doKnockback.cancelCurrentVelocity)
            entity.velocity = velocity
        else
            entity.velocity += velocity
    }
}
