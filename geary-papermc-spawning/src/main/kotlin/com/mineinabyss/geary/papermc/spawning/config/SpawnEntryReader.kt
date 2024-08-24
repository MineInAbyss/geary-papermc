package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.Yaml
import com.mineinabyss.geary.papermc.MultiEntryYamlReader
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import org.bukkit.plugin.Plugin
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div

class SpawnEntryReader(
    private val plugin: Plugin,
    private val yamlFormat: Yaml,
) {
    private val reader = MultiEntryYamlReader(SpawnEntry.serializer(), yamlFormat)

    fun readSpawnEntries(): List<SpawnEntry> {
        return reader.decodeRecursive((plugin.dataPath / "spawns").createParentDirectories())
            .filter { it.type != SpawnType.None }
    }
}
