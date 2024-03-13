package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
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

fun GearyModule.createDoKnockbackAction() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val doKnockback by source.get<DoKnockback>()
    }
).exec {
    val targetLoc = bukkit.location
    val center = doKnockback.center.get(this)
    if (targetLoc.world != center.world) return@exec

    val delta = center - bukkit.location
    delta.y = 0.0

    val velocity = delta.toVector().multiply(-1).normalize().apply {
        y = cos(Math.toRadians(doKnockback.yAngle))
    } * doKnockback.power

    // Not sure about this, should probably get all of the

    if (doKnockback.cancelCurrentVelocity)
        bukkit.velocity = velocity
    else
        bukkit.velocity += velocity

}
