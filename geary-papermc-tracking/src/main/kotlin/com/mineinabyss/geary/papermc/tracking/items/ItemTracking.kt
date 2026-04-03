package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.features.feature
import com.mineinabyss.geary.addons.world
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.services.SerializableItemStackService
import org.bukkit.entity.Player
import org.kodein.di.bindSingletonOf
import org.kodein.di.delegate
import org.kodein.di.instance

val ItemTracking = feature<ItemTrackingModule>("item-tracking") {
    dependsOn {
        condition {
            require(instance<GearyPaperConfig>().items.enabled) { "Item tracking must be enabled in config" }
        }
    }

    dependencies {
        bindSingletonOf(::GearyItemProvider)
        bindSingletonOf(::NMSBackedItemTracking)
        delegate<ItemTrackingModule>().to<NMSBackedItemTracking>()
    }

    onEnable {
        val itemTracking = instance<ItemTrackingModule>()
        world {
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
        Services.get<SerializableItemStackService>().registerProvider("") { item, prefabName ->
            val result = itemTracking.createItem(PrefabKey.of(prefabName), item)
            result != null
        }
    }
}
