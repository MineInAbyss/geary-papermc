@file:UseSerializers(ColorSerializer::class, IntRangeSerializer::class, DoubleRangeSerializer::class)

package com.mineinabyss.geary.papermc.bridge.actions

import com.destroystokyo.paper.ParticleBuilder
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.expr
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle

@Serializable
@SerialName("geary:particle")
class ParticleAction(
    val at: Expression<@Contextual Location>,
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
        ParticleBuilder(eval(particle))
            .location(eval(at))
            .offset(eval(offsetX), eval(offsetY), eval(offsetZ))
            .color(eval(color))
            .count(eval(count))
            .extra(eval(speed))
            .receivers(eval(radius))
            .spawn()
    }
}
