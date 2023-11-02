package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.*
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity

val gearyMobs by DI.observe<EntityTracking>()

interface EntityTracking {
    val bukkitEntityComponent: ComponentId
    val bukkit2Geary: BukkitEntity2Geary
    val prefabs: GearyMobPrefabQuery

    companion object : GearyAddonWithDefault<EntityTracking> {
        override fun default(): EntityTracking = object : EntityTracking {
            override val bukkitEntityComponent = componentId<BukkitEntity>()
            override val bukkit2Geary = BukkitEntity2Geary()
            override val prefabs = GearyMobPrefabQuery()
        }

        override fun EntityTracking.install() {
            geary.pipeline.addSystems(
                TrackOnSetBukkitComponent(),
                UntrackOnRemoveBukkitComponent(),
                AttemptSpawnListener(),
                AttemptSpawnMythicMob()
            )
            geary.pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(EntityWorldEventTracker())
            }
        }
    }
}
