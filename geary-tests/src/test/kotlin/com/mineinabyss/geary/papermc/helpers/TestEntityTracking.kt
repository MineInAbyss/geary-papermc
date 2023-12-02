package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking

class TestEntityTracking {
    companion object : GearyAddonWithDefault<EntityTracking> by EntityTracking {
        override fun default() = object : EntityTracking by EntityTracking.default() {
            override val bukkit2Geary = BukkitEntity2Geary(forceMainThread = false)
        }
    }
}
