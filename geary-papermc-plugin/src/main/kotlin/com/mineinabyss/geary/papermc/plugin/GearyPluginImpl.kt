package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.GearyArchetypeModule
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.dsl.FileSystemAddon
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.uuid.UUIDTracking
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.serialization.formats.YamlFormat
import kotlinx.coroutines.delay
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
        val gearyModule = object : GearyArchetypeModule(tickDuration = 1.ticks) {
            override val engine: ArchetypeEngine = PaperMCEngine()
            override val logger = Logger(StaticConfig(logWriterList = listOf(PaperWriter(this@GearyPluginImpl))))
        }
        val paperModule = GearyPaperModule(this)

        DI.add<GearyModule>(gearyModule)
        DI.add<GearyArchetypeModule>(gearyModule)
        DI.add<GearyPaperModule>(paperModule)
        gearyModule.inject()

        // Auto register Bukkit listeners when they are added as a system
        geary.pipeline.interceptSystemAddition { system ->
            if (system is Listener) listeners(system)
            system
        }

        Logger.setMinSeverity(paperModule.config.logLevel)

        // Configure geary and install some reasonable default addons for paper
        geary {
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(UUIDTracking)

            if (paperModule.config.trackEntities) install(EntityTracking)
            if (paperModule.config.trackItems) install(ItemTracking)

            serialization {
                format("yml", ::YamlFormat)

                components {
                    component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
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
                gearyModule.start()
                Bukkit.getOnlinePlayers().forEach { it.toGeary() }
            }
        }

        // Run startup pipeline
        launch {
            delay(1.ticks) // Waits until first tick has complete (all plugins loaded)
            geary.pipeline.runStartupTasks()
        }

        // Register commands
        GearyCommands()
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
