package com.mineinabyss.geary.papermc.bridge.actions.particles

import com.destroystokyo.paper.ParticleBuilder
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
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
class DoSpawnParticle(
    val at: Input<@Contextual Location>,
    val particle: Particle,
    val offsetX: DoubleRange = 0.0..0.0,
    val offsetY: DoubleRange = 0.0..0.0,
    val offsetZ: DoubleRange = 0.0..0.0,
    val color: @Serializable(with = ColorSerializer::class) Color? = null,
    val count: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
    val radius: Int = 32,
    val speed: DoubleRange = 0.0..0.0,
)

fun GearyModule.createSpawnParticleAction() = listener(
    object : ListenerQuery() {
        val spawn by source.get<DoSpawnParticle>()
    }
).exec {
    val location = spawn.at.get(this)
    with(spawn) {
        ParticleBuilder(particle)
            .location(location)
            .offset(offsetX.randomOrMin(), offsetY.randomOrMin(), offsetZ.randomOrMin())
            .color(color)
            .count(count.randomOrMin())
            .extra(speed.randomOrMin())
            .receivers(radius)
            .spawn()
    }
}
