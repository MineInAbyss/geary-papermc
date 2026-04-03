package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.features.addCloseables
import com.mineinabyss.features.feature
import com.mineinabyss.features.get
import com.mineinabyss.geary.addons.world
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.CatchType
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.entities.components.BindToEntityType
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.GearyPlayerTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntityRemoveListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntitySetListener
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit
import org.kodein.di.bindSingleton
import org.kodein.di.bindSingletonOf

class EntityTrackingQueries(world: Geary) : WorldScoped by world.newScope() {
    val entityTypeBinds = cacheGroupedBy(query<BindToEntityType>()) { (type) ->
        entity.addRelation<NoInherit, BindToEntityType>()
        type.key
    }
}

data class EntityTrackingModule(
    val bukkitEntityComponent: ComponentId,
    val bukkit2Geary: BukkitEntity2Geary,
    val query: GearyMobPrefabQuery,
    val queries: EntityTrackingQueries,
)

val EntityTracking = feature<EntityTrackingModule>("entity-tracking") {
    dependencies {
        bindSingletonOf(::GearyMobPrefabQuery)
        bindSingletonOf(::EntityTrackingQueries)
        bindSingleton { BukkitEntity2Geary(get<GearyPaperConfig>().catch.asyncEntityConversion == CatchType.ERROR, get(), get()) }
        bindSingleton<EntityTrackingModule> {
            val geary = get<Geary>()
            EntityTrackingModule(
                bukkitEntityComponent = geary.componentId<BukkitEntity>(),
                bukkit2Geary = get(),
                query = get(),
                queries = get(),
            )
        }
        bindSingletonOf(::EntityWorldEventTracker)
        bindSingletonOf(::GearyPlayerTracker)
    }

    onEnable {
        val config = get<GearyPaperConfig>().entities
        if (config.trackOtherEntities) listeners(get<EntityWorldEventTracker>())
        if (config.trackPlayers) listeners(get<GearyPlayerTracker>())
        addCloseables(
            get<EntityTrackingQueries>(),
            get<BukkitEntity2Geary>()
        )

        world {
            createBukkitEntityRemoveListener()
            createBukkitEntitySetListener()
            Bukkit.getServer().worlds.forEach { world ->
                world.entities.forEach entities@{ entity ->
                    get<BukkitEntity2Geary>().getOrCreate(entity)
                }
            }
        }
    }
}
