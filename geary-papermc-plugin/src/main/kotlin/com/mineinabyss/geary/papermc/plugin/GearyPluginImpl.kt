package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.geary.papermc.GearyPlugin
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
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.serialization.formats.YamlFormat
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.*
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
        val configModule = GearyPaperConfigModule(this)

        DI.add<GearyPaperConfigModule>(configModule)

        Logger.setMinSeverity(configModule.config.logLevel)

        geary(PaperEngineModule, PaperEngineModule(this)) {
            // Install default addons
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(UUIDTracking)

            if (configModule.config.trackEntities) install(EntityTracking)
            if (configModule.config.trackItems) install(ItemTracking)

            serialization {
                format("yml", ::YamlFormat)

                components {
                    component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
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
                Bukkit.getOnlinePlayers().forEach { it.toGeary() }

                logSuccess("Loaded mob types: ${entityTracking.mobPrefabs.getKeys().joinToString()}")
                // TODO list items
//                logSuccess("Loaded item types: ${itemTracking.mobPrefabs.getKeys().joinToString()}")
            }
        }

        // Register commands
        GearyCommands()
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
