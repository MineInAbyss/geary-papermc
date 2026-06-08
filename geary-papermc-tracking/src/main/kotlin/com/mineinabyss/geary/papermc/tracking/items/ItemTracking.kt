package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.addons.gearyAddon
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.papermc.services.GearyItemService
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.migration.createItemMigrationListener
import com.mineinabyss.geary.papermc.tracking.items.systems.createInventoryTrackerSystem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.plugin
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.services.SerializableItemStackService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val MCItemTracking = module("minecraft-item-tracking") {
    require(get<GearyPaperConfig>().items.enabled) { "Item tracking must be enabled in config" }

    gearyWorld {
        world.install(ItemTracking)
    }

    val itemService = object : GearyItemService {
        override fun getItem(namespace: String, key: String): ItemStack? {
            return gearyPaper.features[ItemTracking].createItem(PrefabKey.of(namespace, key), null)
        }

        override fun getItem(namespaceKey: String): ItemStack? {
            return gearyPaper.features[ItemTracking].createItem(PrefabKey.of(namespaceKey), null)
        }
    }
    Services.register(plugin, itemService)
    addCloseable { Bukkit.getServer().servicesManager.unregister(GearyItemService::class.java, itemService) }
}

val ItemTracking = gearyAddon("item-tracking") {
    single { new(::GearyItemProvider) }
    val itemTracking by single { new(::NMSBackedItemTracking) }.and<ItemTrackingModule>()

    Services.getOrNull<SerializableItemStackService>()?.registerProvider("") { item, prefabName ->
        val result = itemTracking.createItem(PrefabKey.of(prefabName), item)
        result != null
    }
    createItemMigrationListener()
    createInventoryTrackerSystem()

    // Create PlayerItemCache on player entities
    observe<OnSet>().involving(query<Player>()).exec { (player) ->
        entity.set<PlayerItemCache<*>>(itemTracking.createCache(entity))
    }
    observe<OnRemove>().involving(query<PlayerItemCache<*>>()).exec { (cache) ->
        cache.clear()
    }
}.gets<ItemTrackingModule>()
