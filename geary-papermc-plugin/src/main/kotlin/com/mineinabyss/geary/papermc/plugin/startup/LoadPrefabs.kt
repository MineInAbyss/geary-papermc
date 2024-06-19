package com.mineinabyss.geary.papermc.plugin.startup

import com.mineinabyss.geary.addons.get
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.namespace
import com.mineinabyss.geary.prefabs.prefabs
import okio.Path.Companion.toOkioPath
import org.bukkit.plugin.Plugin
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

fun GearyModule.loadPrefabsInPluginFolder() {
    // Load prefabs in Geary folder, each subfolder is considered its own namespace
    get<Plugin>().dataFolder.toPath().listDirectoryEntries()
        .filter { it.isDirectory() }
        .forEach { folder ->
            namespace(folder.name) {
                this@loadPrefabsInPluginFolder.logger.i("Loading prefabs from $folder")
                prefabs {
                    fromRecursive(folder.toOkioPath())
                }
            }
        }
}
