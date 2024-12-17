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
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Color
import org.bukkit.Particle

@Serializable
@SerialName("geary:particle")
class ParticleAction(
    val particle: Expression<Particle>,
    val spreadX: Expression<Double?> = expr(null),
    val spreadY: Expression<Double?> = expr(null),
    val spreadZ: Expression<Double?> = expr(null),
    /** Default value for spread x,y,z, setting those will override this value. */
    val spread: Expression<Double?> = expr(null),
    val offset: Expression<List<Double>> = expr(listOf(0.0, 0.0, 0.0)),
    val color: Expression<Color?> = expr(null),
    val item: Expression<SerializableItemStack?> = expr(null),
    val count: Expression<Int> = expr(1),
    val radius: Expression<Int> = expr(32),
    val speed: Expression<Double> = expr(0.0),
) : Action {
    override fun ActionGroupContext.execute() {
        val offset = eval(offset)
        //TODO cleaner serializer for Bukkit's Vector class
        val location = (location ?: return).add(offset[0], offset[1], offset[2])
        ParticleBuilder(eval(particle))
            .location(location)
            .apply {
                // We check if offset is set, then re-evaluate it 3 times in case it's using a random value
                if (eval(spread) != null) offset(
                    eval(spreadX) ?: eval(spread) ?: 0.0,
                    eval(spreadY) ?: eval(spread) ?: 0.0,
                    eval(spreadZ) ?: eval(spread) ?: 0.0,
                )
                else offset(eval(spreadX) ?: 0.0, eval(spreadY) ?: 0.0, eval(spreadZ) ?: 0.0)
            }
            .color(eval(color))
            .count(eval(count))
            .extra(eval(speed))
            .receivers(eval(radius))
            .data(eval(item)?.toItemStackOrNull())
            .spawn()
    }
}
