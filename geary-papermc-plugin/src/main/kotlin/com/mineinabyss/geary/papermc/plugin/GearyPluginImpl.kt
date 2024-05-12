package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.bridge.events.GearyPaperMCBridge
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.features.entities.bucketable.BucketableListener
import com.mineinabyss.geary.papermc.features.entities.displayname.ShowDisplayNameOnKillerListener
import com.mineinabyss.geary.papermc.features.entities.prevent.PreventEventsFeature
import com.mineinabyss.geary.papermc.features.entities.sounds.AmbientSoundsFeature
import com.mineinabyss.geary.papermc.features.entities.taming.TamingListener
import com.mineinabyss.geary.papermc.features.general.cooldown.CooldownFeature
import com.mineinabyss.geary.papermc.features.items.backpack.BackpackListener
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDropListener
import com.mineinabyss.geary.papermc.features.items.holdsentity.SpawnHeldPrefabSystem
import com.mineinabyss.geary.papermc.features.items.nointeraction.DisableItemInteractionsListener
import com.mineinabyss.geary.papermc.features.items.recipes.ItemRecipes
import com.mineinabyss.geary.papermc.features.items.wearables.WearableItemSystem
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.mythicmobs.MythicMobsSupport
import com.mineinabyss.geary.papermc.plugin.commands.GearyCommands
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.blocks.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.FileSystemAddon
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
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.plugin.dataPath
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializablePrefabItemService
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        // Register DI
        val configModule = object : GearyPaperModule {
            override val plugin: JavaPlugin = this@GearyPluginImpl
            override val configHolder = config(
                "config", plugin.dataPath, GearyPaperConfig(),
                onLoad = { plugin.injectLogger(ComponentLogger.forPlugin(plugin, minSeverity = it.logLevel)) }
            )

            override val config: GearyPaperConfig by configHolder

            override val logger by plugin.observeLogger()
        }

        DI.add<GearyPaperModule>(configModule)

        geary(PaperEngineModule, PaperEngineModule(this)) {
            // Install default addons
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(UUIDTracking, SynchronizedUUID2GearyMap())

            if (configModule.config.trackEntities) install(EntityTracking)
            if (configModule.config.items.enabled) install(ItemTracking)
            if (configModule.config.trackBlocks) install(BlockTracking)

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

            // Load prefabs in Geary folder, each subfolder is considered its own namespace
            dataFolder.toPath().listDirectoryEntries()
                .filter { it.isDirectory() }
                .forEach { folder ->
                    namespace(folder.name) {
                        geary.logger.i("Loading prefabs from $folder")
                        prefabs {
                            fromRecursive(folder.toOkioPath())
                        }
                    }
                }

            // Start engine ticking
            on(ENABLE) {
                if (configModule.config.trackEntities) {
                    server.worlds.forEach { world ->
                        world.entities.forEach entities@{ entity ->
                            gearyMobs.bukkit2Geary.getOrCreate(entity)
                        }
                    }
                }
                gearyPaper.logger.s(
                    "Loaded prefabs - Mobs: ${gearyMobs.query.prefabs.getKeys().size}, Blocks: ${gearyBlocks.prefabs.getKeys().size}, Items: ${gearyItems.prefabs.getKeys().size}"
                )
            }

            val isMMLoaded = Bukkit.getPluginManager().plugins.find { it.name == "MythicMobs" } != null
            if (isMMLoaded && gearyPaper.config.integrations.mythicMobs) {
                gearyPaper.logger.s("MythicMobs detected, enabling support.")
                install(MythicMobsSupport)
            }
            install(ItemRecipes)
            install(GearyPaperMCBridge)
        }


        DI.add<SerializablePrefabItemService>(
            object : SerializablePrefabItemService {
                override fun encodeFromPrefab(item: ItemStack, prefabName: String) {
                    val result = gearyItems.createItem(PrefabKey.of(prefabName), item)
                    require(result != null) { "Failed to create serializable ItemStack from $prefabName, does the prefab exist and have a geary:set.item component?" }
                }
            })

        // Register commands
        GearyCommands()
    }

    override fun onEnable() {
        ArchetypeEngineModule.start(DI.get<PaperEngineModule>())

        if (gearyPaper.config.trackEntities) {
            geary {
                install(CooldownFeature)
                install(AmbientSoundsFeature)
                install(PreventEventsFeature)
            }
            listeners(
                BucketableListener(),
                ShowDisplayNameOnKillerListener(),
                TamingListener(),
            )
        }

        if (gearyPaper.config.items.enabled) {
            listeners(
                WearableItemSystem(),
                BackpackListener(),
                SpawnHeldPrefabSystem(),
                DisableItemInteractionsListener(),
                ReplaceBurnedDropListener(),
            )
        }
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
