package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.onPluginEnable
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery
import com.mineinabyss.geary.papermc.tracking.items.inventory.InventoryCacheWrapper
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.LoginListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.inventory.ItemStack

interface ItemTrackingModule {
    val itemProvider: GearyItemProvider
    val loginListener: LoginListener
    val prefabs: CachedQuery<GearyItemPrefabQuery>
    fun getCacheWrapper(entity: GearyEntity): InventoryCacheWrapper?

    fun createItem(
        prefabKey: PrefabKey,
        writeTo: ItemStack? = null,
    ): ItemStack? = itemProvider.serializePrefabToItemStack(prefabKey, writeTo)
}

val ItemTracking = createAddon<ItemTrackingModule>("Item Tracking", { NMSBackedItemTracking(geary) }) {
    onStart {
        createItemMigrationListener()
        createInventoryTrackerSystem()
    }
    onPluginEnable {
        plugin.listeners(configuration.loginListener)
    }
}
