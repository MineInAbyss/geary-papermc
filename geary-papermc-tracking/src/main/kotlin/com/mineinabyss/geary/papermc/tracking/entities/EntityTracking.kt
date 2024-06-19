package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.createAddon
import com.mineinabyss.geary.addons.dependencies
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.queries.QueryGroupedBy
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.CatchType
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.components.BindToEntityType
import com.mineinabyss.geary.papermc.tracking.entities.components.markBindEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.components.markSetEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn.createAttemptSpawnListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntityRemoveListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntitySetListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.removevanillamobs.RemoveVanillaMobsListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.ConvertEntityTypesListener
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity

val gearyMobs by DI.observe<EntityTrackingModule>()

class EntityTrackingConfiguration {
    var forceMainThread: Boolean? = null
}

class EntityTrackingModule(
    val config: EntityTrackingConfiguration,
) {
    val bukkitEntityComponent: ComponentId = componentId<BukkitEntity>()

    val bukkit2Geary: BukkitEntity2Geary = BukkitEntity2Geary(
        config.forceMainThread
            ?: (gearyPaper.config.catch.asyncEntityConversion == CatchType.ERROR)
    )

    val query: GearyMobPrefabQuery = GearyMobPrefabQuery()

    val entityTypeBinds: QueryGroupedBy<String, ShorthandQuery1<BindToEntityType>> =
        geary.cacheGroupedBy(query<BindToEntityType>()) { (type) ->
            entity.addRelation<NoInherit, BindToEntityType>()
            type.key
        }
}

val EntityTracking = createAddon<GearyModule, EntityTrackingConfiguration>(
    createConfiguration = ::EntityTrackingConfiguration,
) {
    application.run {
        dependencies {
            add(EntityTrackingModule(config))
        }
        createBukkitEntityRemoveListener()
        createBukkitEntitySetListener()
        createAttemptSpawnListener()
        markSetEntityTypeAsCustomMob()
        markBindEntityTypeAsCustomMob()

        onPluginEnable {
            listeners(
                EntityWorldEventTracker(),
                ConvertEntityTypesListener(),
                RemoveVanillaMobsListener(),
            )
        }
    }
}
