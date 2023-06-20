package com.mineinabyss.geary.papermc.bridge.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.coroutines.delay
import org.bukkit.util.Vector
import kotlin.time.Duration

/**
 * Freezes an entity in place in the world for the given duration.
 */
fun GearyEntity.freeze(
    length: Duration,
    entity: BukkitEntity? = get()
): Boolean {
    entity ?: return false

    val initialLocation = entity.location
    var timePassed = 0L
    var lastTime = System.currentTimeMillis()
    gearyPaper.plugin.launch {
        entity.teleport(initialLocation)
        entity.velocity = Vector(0, 0, 0)
        if (timePassed < length.inWholeTicks && entity.fireTicks == 0) {
            // There's definitely a better way to do this but this should
            // probably work for now
            timePassed += System.currentTimeMillis() - lastTime
            lastTime = System.currentTimeMillis()
            delay(1.ticks)
        }
    }
    return true
}
