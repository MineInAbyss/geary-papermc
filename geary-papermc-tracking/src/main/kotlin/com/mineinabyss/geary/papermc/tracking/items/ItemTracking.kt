package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.createAddon
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.inventory.ItemStack

val gearyItems by geary.di.observe<ItemTrackingModule>()

class ItemTrackingConfig(
    var module: () -> ItemTrackingModule = ::NMSBackedItemTracking,
)

interface ItemTrackingModule {
    val itemProvider: GearyItemProvider
    val loginListener: LoginListener
    val prefabs: CachedQuery<GearyItemPrefabQuery>
    fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper?

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null
    ): ItemStack? = itemProvider.serializePrefabToItemStack(prefabKey, writeTo)
}

val ItemTracking = createAddon<GearyModule, ItemTrackingConfig>(
    createConfiguration = ::ItemTrackingConfig,
) {
    application.run {
        createItemMigrationListener()
        createInventoryTrackerSystem()

        val itemTracking = config.module()
        geary.di.add(itemTracking)

        pipeline.runOnOrAfter(GearyPhase.ENABLE) {
            gearyPaper.plugin.listeners(
                itemTracking.loginListener,
            )
        }
    }
}
