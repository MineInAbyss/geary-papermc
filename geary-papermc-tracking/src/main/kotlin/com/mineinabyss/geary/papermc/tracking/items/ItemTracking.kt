package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.migration.CustomModelDataToPrefabTracker
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.systems.InventoryTrackerSystem
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.inventory.ItemStack

val itemTracking by DI.observe<ItemTracking>()

interface ItemTracking {
    val provider: GearyItemProvider
    val migration: ItemMigration

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null
    ): ItemStack? = provider.serializePrefabToItemStack(prefabKey, writeTo)

    companion object : GearyAddonWithDefault<ItemTracking> {
        override fun default(): ItemTracking = object : ItemTracking {
            override val provider = GearyItemProvider()
            override val migration: ItemMigration = ItemMigration()
        }

        override fun ItemTracking.install() {
            gearyPaper.plugin.listeners(LoginListener())
            geary.pipeline.addSystems(
                InventoryTrackerSystem(),
//                PeriodicSaveSystem(),
                CustomModelDataToPrefabTracker()
            )
        }
    }
}
