@file:UseSerializers(ColorSerializer::class, IntRangeSerializer::class, DoubleRangeSerializer::class)

package com.mineinabyss.geary.papermc.features.common.actions

import com.destroystokyo.paper.ParticleBuilder
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.expr
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Color
import org.bukkit.Particle

@Serializable
@SerialName("geary:particle")
class ParticleAction(
    val particle: Expression<Particle>,
    val offsetX: Expression<Double> = expr(0.0),
    val offsetY: Expression<Double> = expr(0.0),
    val offsetZ: Expression<Double> = expr(0.0),
    val color: Expression<Color?> = expr(null),
    val count: Expression<Int> = expr(1),
    val radius: Expression<Int> = expr(32),
    val speed: Expression<Double> = expr(0.0),
) : Action {
    override fun ActionGroupContext.execute() {
        val location = location ?: return
        ParticleBuilder(eval(particle))
            .location(location)
            .offset(eval(offsetX), eval(offsetY), eval(offsetZ))
            .color(eval(color))
            .count(eval(count))
            .extra(eval(speed))
            .receivers(eval(radius))
            .spawn()
    }
}
