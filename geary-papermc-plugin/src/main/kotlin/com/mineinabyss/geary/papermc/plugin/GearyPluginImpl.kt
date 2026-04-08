package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.actions.GearyActions
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.*
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.features.entities.MinecraftFeatures
import com.mineinabyss.geary.papermc.features.items.CustomItemsFeature
import com.mineinabyss.geary.papermc.features.items.recipes.RecipeFeature
import com.mineinabyss.geary.papermc.features.prefabs.PrefabsFeature
import com.mineinabyss.geary.papermc.features.resourcepacks.ResourcepackGeneratorFeature
import com.mineinabyss.geary.papermc.mythicmobs.MythicMobsFeature
import com.mineinabyss.geary.papermc.plugin.commands.DebugFeature
import com.mineinabyss.geary.papermc.plugin.commands.TestingFeature
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.tracking.blocks.BlockTracking
import com.mineinabyss.geary.papermc.tracking.entities.MCEntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.MCItemTracking
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.dsl.withCommonComponentNames
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.uuid.SynchronizedUUID2GearyMap
import com.mineinabyss.geary.uuid.UUID2GearyMap
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.features.MainCommand
import com.mineinabyss.idofront.features.MainCommandFeature
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.LocationSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class GearyPluginImpl : JavaPlugin(), GearyPlugin, DI {
    override val di: DIContext = DI {
        single<Plugin> { this@GearyPluginImpl }
        singleConfig<GearyPaperConfig>("config.yml") { default = GearyPaperConfig() }
        single<ComponentLogger> { ComponentLogger.forPlugin(get(), minSeverity = get<GearyPaperConfig>().logLevel) }.and<Logger>()
        single { new(::WorldManager) }
        single {
            MainCommand(
                names = listOf("geary"),
                description = null,
                reloadCommandName = "reload",
                reloadCommandPermission = "geary.admin.reload",
                reloadableFeatures = listOf(
                    PrefabsFeature,
                    ResourcepackGeneratorFeature
                )
            )
        }
    }


    override val config: GearyPaperConfig = get()
    override val logger: ComponentLogger = get()
    override val features: DIScope = get()
    override val worldManager: WorldManager = get<WorldManager>()
    private val geary = geary(PaperEngineModule(logger, di.get<GearyPaperConfig>()))
    val globalGeary by geary.getLazy<Geary>()

    override fun onLoad() {
        with(geary) {
            // Install default addons
            install(UUIDTracking.override {
                single<UUID2GearyMap>(ignoreOverride = true) { SynchronizedUUID2GearyMap() }
            })

            serialization {
                formats.registerFormat("yml", ::YamlFormat)
                withUUIDSerializer()
                registerComponentSerializers(
                    Location::class to LocationSerializer.withSerialName("geary:location")
                )
                withCommonComponentNames()
            }

            autoscan {
                scan(this@GearyPluginImpl.classLoader, listOf("com.mineinabyss.geary")) {
                    components()
                }
            }

            install(Prefabs)
        }

        // Register DI
        worldManager.setGlobalEngine(globalGeary)
        GearyPlugin.instance = this
    }

    override fun onEnable() {
        val geary = worldManager.global
        // Run init steps registered by other plugins in onLoad. In the future this would be done per-world
        geary.configure { this@GearyPluginImpl.worldManager.initSteps.forEach { it() } }


        di.scope.loadCatching(module("geary-actions-papermc") {
            require(get<GearyPaperConfig>().actions) { "Actions must be enabled in config" }
            gearyWorld {
                world.install(GearyActions)
            }
        })
        di.scope.loadAllCatching(
            MCEntityTracking,
            MCItemTracking,
            BlockTracking,
            PrefabsFeature,
            MinecraftFeatures,
            ResourcepackGeneratorFeature,
            CustomItemsFeature,
            RecipeFeature,
            SpawningFeature,
            MythicMobsFeature,
            DebugFeature,
            TestingFeature,
        )
        di.scope.load(MainCommandFeature)

        // Start engine ticking
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            geary.tick()
        }, 0, 1)
    }

    override fun onDisable() {
        val geary = worldManager.global
        geary.get<DIScope>().close()
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

    override fun configure(builder: WorldScoped.() -> Unit): AutoCloseable {
        return globalGeary.newScope().apply(builder)
    }

    override fun forEachWorld(builder: Geary.() -> Unit) {
        builder(globalGeary)
    }
}