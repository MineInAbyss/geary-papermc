package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.actions.GearyActions
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.UninitializedGearyModule
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
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.FileSystem
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.uuid.SynchronizedUUID2GearyMap
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.injectLogger
import com.mineinabyss.idofront.messaging.observeLogger
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializablePrefabItemService
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
            override val gearyModule: UninitializedGearyModule = geary(PaperEngineModule())
            override val worldManager = WorldManager()
        }

        DI.add<GearyPaperModule>(configModule)

        gearyPaper.configure {
            // Install default addons
            install(FileSystem.withConfig { okio.FileSystem.SYSTEM })
            install(UUIDTracking.withConfig { SynchronizedUUID2GearyMap() })

            val mobs = if (configModule.config.trackEntities) install(EntityTracking) else null
            val items = if (configModule.config.items.enabled) install(ItemTracking) else null
            val blocks = if (configModule.config.trackBlocks) install(BlockTracking) else null

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
            install(Prefabs)
            dataPath.resolve("prefabs").createDirectories().listDirectoryEntries().filter(Path::isDirectory)
                .forEach { folder ->
                    namespace(folder.name) {
                        prefabs {
                            fromRecursive(folder.toOkioPath())
                        }
                    }
                }

            install(GearyActions)
            install(GearyPaperMCFeatures)

            install("PaperMC init") {
                components {
                    getAddonOrNull(items)?.let { addon ->
                        DI.add<SerializablePrefabItemService>(object : SerializablePrefabItemService {
                            override fun encodeFromPrefab(item: ItemStack, prefabName: String): ItemStack {
                                val result = addon.createItem(PrefabKey.of(prefabName), item)
                                require(result != null) { "Failed to create serializable ItemStack from $prefabName, does the prefab exist and have a geary:set.item component?" }
                                return result
                            }
                        })
                    }
                }

                onStart {
                    getAddonOrNull(mobs)?.let {
                        server.worlds.forEach { world ->
                            world.entities.forEach entities@{ entity ->
                                it.bukkit2Geary.getOrCreate(entity)
                            }
                        }
                    }

                    gearyPaper.logger.s(
                        """Loaded prefabs
                            | mobs: ${getAddonOrNull(mobs)?.query?.prefabs?.count() ?: "disabled"}
                            | blocks: ${getAddonOrNull(blocks)?.prefabs?.count() ?: "disabled"}
                            | items: ${getAddonOrNull(items)?.prefabs?.count() ?: "disabled"}""".replaceIndentByMargin(",")
                            .replace("\n", "")
                    )
                }
            }
        }

        registerGearyCommands()
    }

    override fun onEnable() {
        //TODO api for registering geary per world once we have per world ticking
        gearyPaper.worldManager.setGlobalEngine(gearyPaper.gearyModule.start())
        features.loadAll()
        features.enableAll()
    }

    override fun onDisable() {
        features.disableAll()
        server.worlds.forEach { world ->
            with(world.toGeary()) {
                world.entities.forEach entities@{ entity ->
                    val gearyEntity = entity.toGearyOrNull() ?: return@entities
                    gearyEntity.encodeComponentsTo(entity)
                    gearyEntity.removeEntity()
                }
            }
        }
        server.scheduler.cancelTasks(this)
    }
}
