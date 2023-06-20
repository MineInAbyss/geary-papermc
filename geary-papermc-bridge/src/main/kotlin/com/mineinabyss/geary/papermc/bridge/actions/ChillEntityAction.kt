package com.mineinabyss.geary.papermc.bridge.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Applies the max freezing effect (from being in powdered snow) to an entity
 * for a number of seconds.
 */
fun GearyEntity.chill(
    length: Duration,
    entity: BukkitEntity? = get()
): Boolean {
    entity ?: return false

    gearyPaper.plugin.launch {
        var timePassed = 0L
        val lastTime = System.currentTimeMillis()
        while (timePassed < length.inWholeTicks) {
            entity.freezeTicks = entity.maxFreezeTicks
            timePassed += System.currentTimeMillis() - lastTime
            delay(1.ticks)
        }
    }
    return true
}
