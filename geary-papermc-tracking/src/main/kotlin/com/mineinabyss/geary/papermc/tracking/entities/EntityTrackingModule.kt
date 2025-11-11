package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.queries.QueryGroupedBy
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.CatchType
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.onPluginEnable
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule.Builder
import com.mineinabyss.geary.papermc.tracking.entities.components.BindToEntityType
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.papermc.tracking.entities.systems.EntityWorldEventTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.GearyPlayerTracker
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntityRemoveListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.createBukkitEntitySetListener
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.typealiases.BukkitEntity

//val gearyMobs by DI.observe<EntityTracking>()

data class EntityTrackingModule(
    val bukkitEntityComponent: ComponentId,
    val bukkit2Geary: BukkitEntity2Geary = BukkitEntity2Geary(gearyPaper.config.catch.asyncEntityConversion == CatchType.ERROR),
    val query: GearyMobPrefabQuery,
    val entityTypeBinds: QueryGroupedBy<String, ShorthandQuery1<BindToEntityType>>,
) {
    data class Builder(
        var bindsQuery: Geary.() -> QueryGroupedBy<String, ShorthandQuery1<BindToEntityType>> = {
            cacheGroupedBy(query<BindToEntityType>()) { (type) ->
                entity.addRelation<NoInherit, BindToEntityType>()
                type.key
            }
        },
        var build: Geary.() -> EntityTrackingModule = {
            EntityTrackingModule(
                bukkitEntityComponent = componentId<BukkitEntity>(),
                query = GearyMobPrefabQuery(this),
                entityTypeBinds = bindsQuery()
            )
        },
    )

}

val EntityTracking: Addon<Builder, EntityTrackingModule> = createAddon<Builder, EntityTrackingModule>("Entity Tracking", { Builder() }) {
    val module = configuration.build(geary)

    onPluginEnable {
        val config = gearyPaper.config.entities

        // Track BukkitEntity component set/remove to internal hashmap
        createBukkitEntityRemoveListener()
        createBukkitEntitySetListener()

        if (config.trackOtherEntities) plugin.listeners(EntityWorldEventTracker(this, module))
        if (config.trackPlayers) plugin.listeners(GearyPlayerTracker(this, module))
    }

    module
}
