package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.AttemptSpawnListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.TrackOnSetBukkitComponent
import com.mineinabyss.geary.papermc.tracking.entities.systems.UntrackOnRemoveBukkitComponent
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

val gearyMobs by DI.observe<EntityTracking>()

interface EntityTracking {
    val bukkit2Geary: BukkitEntity2Geary
    val mobPrefabs: GearyMobPrefabQuery
    val itemPrefabs: GearyItemPrefabQuery

    companion object : GearyAddonWithDefault<EntityTracking> {
        override fun default(): EntityTracking = object : EntityTracking {
            override val bukkit2Geary = BukkitEntity2Geary()
            override val mobPrefabs = GearyMobPrefabQuery()
            override val itemPrefabs = GearyItemPrefabQuery()
        }

        override fun EntityTracking.install() {
            gearyPaper.plugin.listeners(EntityWorldEventTracker())
            geary.pipeline.addSystems(
                TrackOnSetBukkitComponent(),
                UntrackOnRemoveBukkitComponent(),
                AttemptSpawnListener(),
            )
        }
    }
}
