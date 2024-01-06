package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
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
    val location: Input<@Contextual Location>,
)

class ExplosionSystem : GearyListener() {
    private val Pointers.explosion by get<Explosion>().on(source)

    override fun Pointers.handle() {
        val location = explosion.location.get(this)
        if (explosion.fuseTicks <= 0) location.createExplosion(
            explosion.power, explosion.setFire, explosion.breakBlocks
        )
        else //only spawn a tnt in if we have a fuse
            location.spawn<TNTPrimed> {
                fuseTicks = explosion.fuseTicks
            }
    }
}
