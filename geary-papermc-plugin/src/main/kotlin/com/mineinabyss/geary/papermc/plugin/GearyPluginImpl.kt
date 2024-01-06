package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.GearyProductionPaperConfigModule
import com.mineinabyss.geary.papermc.bridge.PaperBridge
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.dsl.FileSystemAddon
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializablePrefabItemService
import com.mineinabyss.serialization.formats.YamlFormat
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        // Register DI
        val configModule = GearyProductionPaperConfigModule(this)

        DI.add<GearyPaperConfigModule>(configModule)

        geary(PaperEngineModule, PaperEngineModule(this)) {
            // Install default addons
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(UUIDTracking)

            if (configModule.config.trackEntities) install(EntityTracking)
            if (configModule.config.trackItems) install(ItemTracking)
            if (configModule.config.trackBlocks) install(BlockTracking)
            if (configModule.config.bridgeEvents) install(PaperBridge)

            serialization {
                format("yml", ::YamlFormat)
                withUUIDSerializer()

                components {
                    component(Location::class, LocationSerializer.withSerialName("geary:location"))
                }
                //TODO option to auto register contextual on geary end
                module {
                    contextual(Location::class, LocationSerializer.withSerialName("geary:location"))
                }
            }

            autoscan(classLoader, "com.mineinabyss.geary") {
                components()
            }

            // Auto register Bukkit listeners when they are added as a system
            geary.pipeline.interceptSystemAddition { system ->
                if (system is Listener) listeners(system)
                system
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

                logSuccess(
                    "Loaded types: ${gearyMobs.prefabs.getKeys().size} mobs, ${
                        gearyBlocks.prefabs.getKeys().size
                    } blocks, ${gearyItems.prefabs.getKeys().size} items"
                )
            }
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
