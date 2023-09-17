package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.Location
import org.bukkit.entity.TNTPrimed

class ExplosionSystem : GearyListener() {
    private val Pointers.explosion by get<Explosion>().on(target)

    private val Pointers.location by get<Location>().on(event)
    private val Pointers.explode by family { has<Explode>() }.on(event)

    override fun Pointers.handle() {
        if (explosion.fuseTicks <= 0)
            location.createExplosion(
                explosion.power,
                explosion.setFire,
                explosion.breakBlocks
            )
        else //only spawn a tnt in if we have a fuse
            location.spawn<TNTPrimed> { fuseTicks = explosion.fuseTicks }
    }
}
