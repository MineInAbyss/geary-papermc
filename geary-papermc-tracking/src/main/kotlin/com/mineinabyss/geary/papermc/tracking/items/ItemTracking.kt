package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.services.SerializableItemStackService
import org.bukkit.entity.Player
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind

val ItemTracking = feature<ItemTrackingModule>("item-tracking") {
    dependsOn {
        condition("Item tracking must be enabled in config") { get<GearyPaperConfig>().items.enabled }
    }

    scopedModule {
        scopedOf(::GearyItemProvider)
        scopedOf(::NMSBackedItemTracking) bind ItemTrackingModule::class
    }

    configureGeary {
        onEnable {
            val itemTracking = get<ItemTrackingModule>()
            createItemMigrationListener()
            createInventoryTrackerSystem()

            // Create PlayerItemCache on player entities
            observe<OnSet>().involving(query<Player>()).exec { (player) ->
                entity.set<PlayerItemCache<*>>(itemTracking.createCache(entity))
            }
            observe<OnRemove>().involving(query<PlayerItemCache<*>>()).exec { (cache) ->
                cache.clear()
            }
        }
    }

    onEnable {
        val module = get<ItemTrackingModule>()
        Services.get<SerializableItemStackService>().registerProvider("") { item, prefabName ->
            val result = module.createItem(PrefabKey.of(prefabName), item)
            result != null
        }
    }
}
