package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.features.FeatureManager
import com.mineinabyss.features.feature
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
import com.mineinabyss.idofront.features.MainCommandFeature
import com.mineinabyss.idofront.features.bindConfig
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.LocationSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.bindSingletonOf
import org.kodein.di.instance
import kotlin.io.path.div

class GearyPluginImpl : GearyPlugin(), DIAware {
    override val di = DI {
        bindSingleton<Plugin> { this@GearyPluginImpl }
        bindConfig<GearyPaperConfig>("config.yml") { default = GearyPaperConfig() }
        bindSingleton<ComponentLogger> { ComponentLogger.forPlugin(instance(), minSeverity = instance<GearyPaperConfig>().logLevel) }
        bindSingletonOf(::WorldManager)
        bindSingleton {
            MainCommand(names = listOf("geary"), description = null, reloadCommandName = "reload", reloadCommandPermission = "geary.admin.reload")
        }
    }

    val config by instance<GearyPaperConfig>()
    val worldManager by instance<WorldManager>()

    override fun onLoad() {
        val globalGeary = geary(PaperEngineModule(config), extendDI = this@GearyPluginImpl.di) {
            // Install default addons
            install(UUIDTracking.overrideScope {
                bindSingleton<UUID2GearyMap>(overrides = true) { SynchronizedUUID2GearyMap() }
            })


            install(SerializableComponents) {
                formats.registerFormat("yml", ::YamlFormat)
                withUUIDSerializer()
                registerComponentSerializers(
                    Location::class to LocationSerializer.withSerialName("geary:location")
                )
                withCommonComponentNames()
            }

            install(AutoScanAddon) {
                scan(classLoader, listOf("com.mineinabyss.geary")) {
                    components()
                }
            }

            install(Prefabs)

            install(feature("geary-actions-papermc") {
                dependsOn {
                    condition { require(instance<GearyPaperConfig>().actions) { "Actions must be enabled in config" } }
                    features(GearyActions)
                }
            })
        }
        // Register DI
        val configModule = object : GearyPaperModule {
            private val di = globalGeary

            override val plugin = di.instance<Plugin>() as JavaPlugin
            override val config: GearyPaperConfig = di.instance()
            override val logger: ComponentLogger = di.instance()
            override val features: FeatureManager = di.instance()
            override val worldManager: WorldManager = di.instance()
        }
        worldManager.setGlobalEngine(globalGeary)
        //TODO deprecate in favor of koin
        com.mineinabyss.idofront.di.DI.add<GearyPaperModule>(configModule)

        val featureManager = globalGeary.instance<FeatureManager>()
        featureManager.loadAll(
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

        featureManager.load(MainCommandFeature)
    }

    override fun onEnable() {
        val geary = worldManager.global
        // Run init steps registered by other plugins in onLoad. In the future this would be done per-world
        geary.configure { worldManager.initSteps.forEach { it() } }

        geary.instance<FeatureManager>().enableAll()

        // Start engine ticking
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            geary.tick()
        }, 0, 1)
    }

    override fun onDisable() {
        val geary = worldManager.global
        geary.instance<FeatureManager>().disableAll()
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