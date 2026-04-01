package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.features.FeatureManager
import com.mineinabyss.geary.actions.GearyActions
import com.mineinabyss.geary.autoscan.AutoScanAddon
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.*
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.features.entities.MinecraftFeatures
import com.mineinabyss.geary.papermc.features.items.ItemsFeature
import com.mineinabyss.geary.papermc.features.items.recipes.RecipeFeature
import com.mineinabyss.geary.papermc.features.prefabs.PrefabsFeature
import com.mineinabyss.geary.papermc.features.resourcepacks.ResourcepackGeneratorFeature
import com.mineinabyss.geary.papermc.mythicmobs.MythicMobsFeature
import com.mineinabyss.geary.papermc.plugin.commands.DebugFeature
import com.mineinabyss.geary.papermc.plugin.commands.TestingFeature
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.geary.uuid.SynchronizedUUID2GearyMap
import com.mineinabyss.geary.uuid.UUID2GearyMap
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.MainCommand
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.LocationSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.kodein.di.*
import kotlin.io.path.div

class GearyPluginImpl : GearyPlugin() {
    val application = DI {
        bindSingleton<Plugin> { this@GearyPluginImpl }
        bindSingleton<GearyPaperConfig> {
            config<GearyPaperConfig> { default = GearyPaperConfig() }.single(dataPath / "config.yml").read()
        }
        bindSingleton<ComponentLogger> { ComponentLogger.forPlugin(instance(), minSeverity = instance<GearyPaperConfig>().logLevel) }
        delegate<Logger>().to<ComponentLogger>()
        bindSingleton<Geary> { geary(PaperEngineModule(instance())) }
        //TODO api for registering geary per world once we have per world ticking
        bindSingleton<WorldManager> { WorldManager().apply { setGlobalEngine(instance()) } }
        bindSingleton {
            MainCommand(names = listOf("geary"), description = null, reloadCommandName = "reload", reloadCommandPermission = "geary.admin.reload")
        }
        bindSingleton {
            FeatureManager(di).apply {
                loadAll(
                    EntityTracking,
                    ItemTracking,
                    BlockTracking,
                    PrefabsFeature,
                    MinecraftFeatures,
                    ResourcepackGeneratorFeature,
                    ItemsFeature,
                    RecipeFeature,
                    SpawningFeature,
                    MythicMobsFeature,
                    DebugFeature,
                    TestingFeature,
                )
            }
        }
    }

    val features by application.instance<FeatureManager>()
    val engine by application.instance<Geary>()

    override fun onLoad() {
        // Register DI
        val configModule = object : GearyPaperModule {
            private val app = this@GearyPluginImpl.application.direct

            override val plugin = app.instance<Plugin>() as JavaPlugin
            override val config: GearyPaperConfig = app.instance()
            override val logger: ComponentLogger = app.instance()
            override val features: FeatureManager = app.instance()
            override val worldManager: WorldManager = app.instance()
        }
        //TODO deprecate in favor of koin
        com.mineinabyss.idofront.di.DI.add<GearyPaperModule>(configModule)

        gearyPaper.configure {
            // Install default addons
            install(UUIDTracking.overrideScope {
                bindSingleton<UUID2GearyMap> { SynchronizedUUID2GearyMap() }
            })


            install(SerializableComponents).apply {
                formats.registerFormat("yml", ::YamlFormat)
                withUUIDSerializer()
                registerComponentSerializers(
                    Location::class to LocationSerializer.withSerialName("geary:location")
                )
                withCommonComponentNames()
            }

            install(AutoScanAddon).scan(classLoader, listOf("com.mineinabyss.geary")) {
                components()
            }

            install(Prefabs)

            if (configModule.config.actions) install(GearyActions)

        }
    }

    override fun onEnable() {
        // Run init steps registered by other plugins in onLoad. In the future this would be done per-world
        gearyPaper.worldManager.global.configure {
            gearyPaper.worldManager.initSteps.forEach { it() }
        }
        features.enableAll()
//        gearyPaper.logger.s(
//            """Loaded prefabs
//                            | mobs: ${getAddonOrNull(EntityTracking)?.query?.prefabs?.count() ?: "disabled"}
//                            | blocks: ${getAddonOrNull(BlockTracking)?.prefabs?.count() ?: "disabled"}
//                            | items: ${getAddonOrNull(ItemTracking)?.prefabs?.count() ?: "disabled"}""".replaceIndentByMargin(
//                ","
//            )
//                .replace("\n", "")
//        )

        // Stat engine ticking
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            engine.tick()
        }, 0, 1)
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