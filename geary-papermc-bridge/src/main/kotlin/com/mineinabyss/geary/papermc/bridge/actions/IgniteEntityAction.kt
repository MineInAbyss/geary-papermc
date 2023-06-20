package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlin.time.Duration

/**
 * Sets an entity on fire for a number of seconds.
 */
fun GearyEntity.ignite(length: Duration, entity: BukkitEntity? = get()): Boolean {
    entity ?: return false
    entity.fireTicks = length.inWholeTicks.toInt()
    return true
}
