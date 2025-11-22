package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking

val TestEntityTracking = EntityTracking.overrideScope {
    scoped { BukkitEntity2Geary(forceMainThread = false, get(), get()) }
}
