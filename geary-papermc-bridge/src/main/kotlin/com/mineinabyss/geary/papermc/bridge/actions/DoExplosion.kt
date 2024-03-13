package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.spawning.spawn
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.TNTPrimed

@Serializable
@SerialName("geary:explode")
data class Explosion(
    val power: Float = 4F,
    val setFire: Boolean = false,
    val breakBlocks: Boolean = false,
    val fuseTicks: Int = 0,
    val at: Input<@Contextual Location>,
)

fun GearyModule.createExplosionAction() = listener(
    object : ListenerQuery() {
        val explosion by source.get<Explosion>()
    }
).exec {
    val location = explosion.at.get(this)
    if (explosion.fuseTicks <= 0) location.createExplosion(
        explosion.power, explosion.setFire, explosion.breakBlocks
    )
    else //only spawn a tnt in if we have a fuse
        location.spawn<TNTPrimed> {
            fuseTicks = explosion.fuseTicks
        }

}
