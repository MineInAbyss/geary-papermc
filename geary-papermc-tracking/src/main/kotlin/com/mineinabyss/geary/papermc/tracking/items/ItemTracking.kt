package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.ItemMigration
import com.mineinabyss.geary.papermc.tracking.items.migration.SetItemIgnoredPropertyListener
import com.mineinabyss.geary.papermc.tracking.items.migration.createCustomModelDataToPrefabTracker
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.papermc.tracking.items.systems.MythicMobDropSystem
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.inventory.ItemStack

val gearyItems by DI.observe<ItemTracking>()

interface ItemTracking {
    val itemProvider: GearyItemProvider
    val migration: ItemMigration
    val loginListener: LoginListener
    val prefabs: CachedQueryRunner<GearyItemPrefabQuery>
    fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper?

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null
    ): ItemStack? = itemProvider.serializePrefabToItemStack(prefabKey, writeTo)

    companion object : GearyAddonWithDefault<ItemTracking> {
        override fun default(): ItemTracking = NMSBackedItemTracking()

        override fun ItemTracking.install() = geary.run {
            createItemMigrationListener()
            createInventoryTrackerSystem()
            createCustomModelDataToPrefabTracker()

            pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(
                    loginListener,
                    SetItemIgnoredPropertyListener(),
                    MythicMobDropSystem()
                )
            }
        }
    }
}
