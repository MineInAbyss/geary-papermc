package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule

class TestEntityTracking {
    companion object : GearyAddonWithDefault<EntityTrackingModule> by EntityTracking {
        override fun default() = object : EntityTrackingModule by EntityTracking.default() {
            override val bukkit2Geary = BukkitEntity2Geary(forceMainThread = false)
        }
    }
}
