package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.actions.GearyActions
import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.*
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.features.GearyPaperMCFeatures
import com.mineinabyss.geary.papermc.features.entities.EntityFeatures
import com.mineinabyss.geary.papermc.features.items.ItemFeatures
import com.mineinabyss.geary.papermc.features.items.recipes.RecipeFeature
import com.mineinabyss.geary.papermc.mythicmobs.MythicMobsFeature
import com.mineinabyss.geary.papermc.plugin.commands.registerGearyCommands
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
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
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializablePrefabItemService
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class GearyPluginImpl : GearyPlugin() {
    val features = Features(
        this,
        ::SpawningFeature,
        ::RecipeFeature,
        ::EntityFeatures,
        ::ItemFeatures,
        ::MythicMobsFeature,
    )

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
            override val features get() = this@GearyPluginImpl.features
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

            // Load prefabs in Geary/prefabs folder, each subfolder is considered its own namespace
            dataPath.resolve("prefabs").createDirectories().listDirectoryEntries().filter(Path::isDirectory)
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

            install(GearyActions)
            install(GearyPaperMCFeatures)
        }

        DI.add<SerializablePrefabItemService>(
            object : SerializablePrefabItemService {
                override fun encodeFromPrefab(item: ItemStack, prefabName: String): ItemStack {
                    val result = gearyItems.createItem(PrefabKey.of(prefabName), item)
                    require(result != null) { "Failed to create serializable ItemStack from $prefabName, does the prefab exist and have a geary:set.item component?" }
                    return result
                }
            })

        features.loadAll()
        registerGearyCommands()
    }

    override fun onEnable() {
        ArchetypeEngineModule.start(DI.get<PaperEngineModule>())

        features.enableAll()
    }

    override fun onDisable() {
        features.disableAll()
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
