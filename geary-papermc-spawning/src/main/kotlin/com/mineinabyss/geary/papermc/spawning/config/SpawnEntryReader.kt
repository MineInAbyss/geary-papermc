package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.Yaml
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.idofront.config.ConfigEntryWithKey
import com.mineinabyss.idofront.config.config
import org.bukkit.plugin.Plugin
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div

class SpawnEntryReader(
    private val plugin: Plugin,
    private val yamlFormat: Yaml,
) {
    private val reader = config<SpawnEntry> {
        format = yamlFormat
    }.multiEntry((plugin.dataPath / "spawns").createParentDirectories())

    fun readSpawnEntries(): List<ConfigEntryWithKey<SpawnEntry>> {
        return reader.read()
            .filter { it.entry.type != SpawnType.None }
    }
}
