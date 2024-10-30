package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule

val TestEntityTracking = EntityTracking.withConfig {
    EntityTrackingModule.Builder().apply {
        build = { build().copy(bukkit2Geary = BukkitEntity2Geary(forceMainThread = false)) }
    }
}
