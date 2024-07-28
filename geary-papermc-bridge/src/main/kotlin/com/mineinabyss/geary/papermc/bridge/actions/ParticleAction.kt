package com.mineinabyss.geary.papermc.bridge.actions

import com.destroystokyo.paper.ParticleBuilder
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle

@Serializable
@SerialName("geary:particle")
class ParticleAction(
    val at: Expression<@Contextual Location>,
    val particle: Particle,
    val offsetX: DoubleRange = 0.0..0.0,
    val offsetY: DoubleRange = 0.0..0.0,
    val offsetZ: DoubleRange = 0.0..0.0,
    val color: @Serializable(with = ColorSerializer::class) Color? = null,
    val count: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
    val radius: Int = 32,
    val speed: DoubleRange = 0.0..0.0,
) : Action {
    override fun ActionGroupContext.execute() {
        ParticleBuilder(particle)
            .location(eval(at))
            .offset(offsetX.randomOrMin(), offsetY.randomOrMin(), offsetZ.randomOrMin())
            .color(color)
            .count(count.randomOrMin())
            .extra(speed.randomOrMin())
            .receivers(radius)
            .spawn()
    }
}
