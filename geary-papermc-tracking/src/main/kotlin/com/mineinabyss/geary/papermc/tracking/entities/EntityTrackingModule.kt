package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.queries.QueryGroupedBy
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.CatchType
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.components.BindToEntityType
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.GearyPlayerTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntityRemoveListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntitySetListener
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit
import org.koin.core.module.dsl.scopedOf

data class EntityTrackingModule(
    val bukkitEntityComponent: ComponentId,
    val bukkit2Geary: BukkitEntity2Geary = BukkitEntity2Geary(gearyPaper.config.catch.asyncEntityConversion == CatchType.ERROR),
    val query: GearyMobPrefabQuery,
    val entityTypeBinds: QueryGroupedBy<String, ShorthandQuery1<BindToEntityType>>,
)

val EntityTracking = feature<EntityTrackingModule>("entity-tracking") {
    scopedModule {
        scopedOf(::GearyMobPrefabQuery)
        scoped {
            val geary = get<Geary>()
            EntityTrackingModule(
                bukkitEntityComponent = geary.componentId<BukkitEntity>(),
                query = get(),
                entityTypeBinds = geary.cacheGroupedBy(geary.query<BindToEntityType>()) { (type) ->
                    entity.addRelation<NoInherit, BindToEntityType>()
                    type.key
                }
            )
        }
        scopedOf(::EntityTrackingModule)
        scopedOf(::EntityWorldEventTracker)
        scopedOf(::GearyPlayerTracker)
    }

    configureGeary {
        onEnable {
            addCloseables(
                createBukkitEntityRemoveListener(),
                createBukkitEntitySetListener(),
            )

            Bukkit.getServer().worlds.forEach { world ->
                world.entities.forEach entities@{ entity ->
                    get<BukkitEntity2Geary>().getOrCreate(entity)
                }
            }
        }
    }

    onEnable {
        val config = get<GearyPaperConfig>().entities
        if (config.trackOtherEntities) listeners(get<EntityWorldEventTracker>())
        if (config.trackPlayers) listeners(get<GearyPlayerTracker>())
    }
}
