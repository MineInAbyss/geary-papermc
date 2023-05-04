package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.migration.CustomModelDataToPrefabTracker
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.systems.InventoryTrackerSystem
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.papermc.tracking.items.systems.PeriodicSaveSystem
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

val itemTracking by DI.observe<ItemTracking>()

interface ItemTracking {
//    val provider: GearyItemProvider
    val migration: ItemMigration

    companion object : GearyAddonWithDefault<ItemTracking> {
        override fun default(): ItemTracking = object : ItemTracking {
            override val provider = GearyItemProvider()
            override val migration: ItemMigration = ItemMigration()
        }

        override fun ItemTracking.install() {
            DI.add(this)
            gearyPaper.plugin.listeners(LoginListener())
            geary.pipeline.addSystems(
                InventoryTrackerSystem(),
                PeriodicSaveSystem(),
                CustomModelDataToPrefabTracker()
            )
        }
    }
}
