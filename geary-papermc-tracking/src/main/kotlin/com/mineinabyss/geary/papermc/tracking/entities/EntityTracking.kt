package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.TrackOnSetBukkitComponent
import com.mineinabyss.geary.papermc.tracking.entities.systems.UntrackOnRemoveBukkitComponent
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

val entityTracking by DI.observe<EntityTracking>()

interface EntityTracking {
    val bukkit2Geary: BukkitEntity2Geary

    companion object : GearyAddonWithDefault<EntityTracking> {
        override fun default(): EntityTracking = object : EntityTracking {
            override val bukkit2Geary = BukkitEntity2Geary()
        }

        override fun EntityTracking.install() {
            gearyPaper.plugin.listeners(EntityWorldEventTracker())
            geary.pipeline.addSystems(TrackOnSetBukkitComponent(), UntrackOnRemoveBukkitComponent())
        }
    }
}
