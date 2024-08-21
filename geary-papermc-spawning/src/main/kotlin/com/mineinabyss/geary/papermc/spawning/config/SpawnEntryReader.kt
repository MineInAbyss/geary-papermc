package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.fileSystem
import com.mineinabyss.geary.serialization.serializableComponents
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import okio.Path.Companion.toOkioPath
import org.bukkit.plugin.Plugin
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div

class SpawnEntryReader(
    val plugin: Plugin,
) {
    val serializer = MapSerializer(String.serializer(), SpawnEntry.serializer())

    fun readSpawnEntries(): List<SpawnEntry> {
        val rootDir = (plugin.dataPath / "spawns").createParentDirectories()
        val yamlFormat = serializableComponents.formats["yml"] ?: error("YAML format not found")
        val spawns = fileSystem
            .listRecursively(rootDir.toOkioPath(), true)
            .filter { it.name.contains('.') }
            .mapNotNull { path ->
                runCatching { yamlFormat.decodeFromFile(serializer, path) }
                    .onFailure { geary.logger.w { "Failed to read spawn entry from $path" } }
                    .getOrNull()
            }
            .flatMap { it.values }
            .toList()
        return spawns
    }
}
