package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.CatchType
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.*
import com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn.createAttemptSpawnListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn.createAttemptSpawnMythicMobListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.removevanillamobs.RemoveVanillaMobsListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.ConvertEntityTypesListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.ConvertToMythicMobListener
import com.mineinabyss.geary.systems.builders.cachedQuery
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit

val gearyMobs by DI.observe<EntityTracking>()

interface EntityTracking {
    val bukkitEntityComponent: ComponentId
    val bukkit2Geary: BukkitEntity2Geary
    val prefabs: CachedQueryRunner<GearyMobPrefabQuery>

    companion object : GearyAddonWithDefault<EntityTracking> {
        override fun default(): EntityTracking = object : EntityTracking {
            override val bukkitEntityComponent = componentId<BukkitEntity>()
            override val bukkit2Geary = BukkitEntity2Geary(gearyPaper.config.catch.asyncEntityConversion == CatchType.ERROR)
            override val prefabs = geary.cachedQuery(GearyMobPrefabQuery())
        }

        override fun EntityTracking.install() {
            geary.createBukkitEntityRemoveListener()
            geary.createBukkitEntitySetListener()
            geary.createAttemptSpawnListener()
            geary.pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(
                    EntityWorldEventTracker(),
                    ConvertEntityTypesListener(),
                    RemoveVanillaMobsListener(),
                )

                if (Bukkit.getPluginManager().plugins.any { it.name == "MythicMobs" }) {
                    geary.createAttemptSpawnMythicMobListener()

                    gearyPaper.plugin.listeners(
                        ConvertToMythicMobListener(),
                    )
                }
            }
        }
    }
}
