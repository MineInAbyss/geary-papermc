package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
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
    val itemProvider: GearyItemProvider
    val migration: ItemMigration
    val loginListener: LoginListener
    fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper?

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null
    ): ItemStack? = itemProvider.serializePrefabToItemStack(prefabKey, writeTo)

    companion object : GearyAddonWithDefault<ItemTracking> {
        override fun default(): ItemTracking = NMSBackedItemTracking()

        override fun ItemTracking.install() {
            gearyPaper.plugin.listeners(loginListener)
            geary.pipeline.addSystems(
                InventoryTrackerSystem(),
                CustomModelDataToPrefabTracker()
            )
        }
    }
}
