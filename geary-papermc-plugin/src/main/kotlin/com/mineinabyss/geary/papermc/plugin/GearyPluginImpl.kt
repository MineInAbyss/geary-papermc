package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.GearyProductionPaperConfigModule
import com.mineinabyss.geary.papermc.bridge.PaperBridge
import com.mineinabyss.geary.papermc.configlang.ConfigLang
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.entityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.dsl.FileSystemAddon
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.serialization.formats.YamlFormat
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        saveDefaultConfig()

        // Register DI
        val configModule = GearyProductionPaperConfigModule(this)

        DI.add<GearyPaperConfigModule>(configModule)

        geary(PaperEngineModule, PaperEngineModule(this)) {
            // Install default addons
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(UUIDTracking)

            if (configModule.config.trackEntities) install(EntityTracking)
            if (configModule.config.trackItems) install(ItemTracking)
            if (configModule.config.bridgeEvents) install(PaperBridge)
            if (configModule.config.configLang) install(ConfigLang)

            serialization {
                format("yml", ::YamlFormat)
                withUUIDSerializer()
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
                Bukkit.getOnlinePlayers().forEach { it.toGeary() }

                logSuccess("Loaded mob types: ${entityTracking.mobPrefabs.getKeys().joinToString()}")
                logSuccess("Loaded item types: ${entityTracking.itemPrefabs.getKeys().joinToString()}")
            }
        }

        // Register commands
        GearyCommands()
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
