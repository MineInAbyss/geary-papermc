package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addons.dependencies
import com.mineinabyss.geary.addons.install
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.GearyPluginConfig
import com.mineinabyss.geary.papermc.PaperEngineModule
import com.mineinabyss.geary.papermc.bridge.events.paperMCBridge
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.features.entityFeatures
import com.mineinabyss.geary.papermc.features.itemFeatures
import com.mineinabyss.geary.papermc.features.items.recipes.itemRecipes
import com.mineinabyss.geary.papermc.mythicmobs.mythicMobsSupport
import com.mineinabyss.geary.papermc.plugin.commands.GearyCommands
import com.mineinabyss.geary.papermc.plugin.startup.loadPrefabsInPluginFolder
import com.mineinabyss.geary.papermc.plugin.startup.trackExistingBukkitEntities
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.geary.uuid.SynchronizedUUID2GearyMap
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.injectLogger
import com.mineinabyss.idofront.plugin.dataPath
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializablePrefabItemService
import okio.FileSystem
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        geary(PaperEngineModule, configure = {
            plugin = this@GearyPluginImpl
            configHolder = config(
                "config", dataPath, GearyPluginConfig(),
                onLoad = { injectLogger(ComponentLogger.forPlugin(this@GearyPluginImpl, minSeverity = it.logLevel)) }
            )
        }) {
            dependencies {
                add<Plugin>(this@GearyPluginImpl)
                add<FileSystem>(FileSystem.SYSTEM)
            }
            // Install default addons
            install(UUIDTracking(SynchronizedUUID2GearyMap()))

            if (config.trackEntities) install(EntityTracking)
            if (config.items.enabled) install(ItemTracking)
            if (config.trackBlocks) install(BlockTracking)
            if (config.enableEventBridge) paperMCBridge()
            if (config.trackEntities) entityFeatures()
            if (config.items.enabled) {
                itemFeatures()
                itemRecipes()
            }

            serialization {
                format("yml", ::YamlFormat)
                withUUIDSerializer()
                withCommonComponentNames()

                components {
                    component(Location::class, LocationSerializer.withSerialName("geary:location"))
                }
                module {
                    contextual(Location::class, LocationSerializer.withSerialName("geary:location"))
                }
            }

            autoscan(classLoader, "com.mineinabyss.geary") {
                components()
            }

            loadPrefabsInPluginFolder()
            trackExistingBukkitEntities()

            val isMMLoaded = Bukkit.getPluginManager().plugins.find { it.name == "MythicMobs" } != null
            if (isMMLoaded && config.integrations.mythicMobs) {
                logger.s("MythicMobs detected, enabling support.")
                mythicMobsSupport()
            }
        }

        DI.add<SerializablePrefabItemService>(
            object : SerializablePrefabItemService {
                override fun encodeFromPrefab(item: ItemStack, prefabName: String): ItemStack {
                    val result = gearyItems.createItem(PrefabKey.of(prefabName), item)
                    require(result != null) { "Failed to create serializable ItemStack from $prefabName, does the prefab exist and have a geary:set.item component?" }
                    return result
                }
            })

        // Register commands
        GearyCommands()
    }

    override fun onEnable() {
        geary.start()
    }

    override fun onDisable() {
        server.worlds.forEach { world ->
            world.entities.forEach entities@{ entity ->
                val gearyEntity = entity.toGearyOrNull() ?: return@entities
                gearyEntity.encodeComponentsTo(entity)
                gearyEntity.removeEntity()
            }
        }
        server.scheduler.cancelTasks(this)
    }
}
