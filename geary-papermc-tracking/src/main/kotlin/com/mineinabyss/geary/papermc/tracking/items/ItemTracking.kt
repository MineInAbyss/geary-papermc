package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.services.SerializableItemStackService
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind

val ItemTracking = feature<ItemTrackingModule>("Item Tracking") {
    dependsOn {
        condition("Item tracking must be enabled in config") { get<GearyPaperConfig>().items.enabled }
    }

    scopedModule {
        scopedOf(::NMSBackedItemTracking) bind ItemTrackingModule::class
    }

    configureGeary {
        onEnable {
            addCloseables(
                createItemMigrationListener(),
                createInventoryTrackerSystem(),
            )
        }
    }

    onEnable {
        val module = get<ItemTrackingModule>()
        Services.get<SerializableItemStackService>().registerProvider("") { item, prefabName ->
            val result = module.createItem(PrefabKey.of(prefabName), item)
            result != null
        }
        listeners(module.loginListener)
    }
}
