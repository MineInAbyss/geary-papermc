package com.mineinabyss.geary.papermc

import co.touchlab.kermit.Severity
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
class GearyPaperConfig(
    val debug: Boolean = false,
    @YamlComment("Convert bukkit entities to and from geary, for instance to store and persist components on a player.")
    val trackEntities: Boolean = true,
    @YamlComment("Convert items to and from geary. Depends on entity tracking.")
    val trackItems: Boolean = true,
    @YamlComment("If an item has no prefabs encoded, try to find its prefab by matching custom model data.")
    val migrateItemCustomModelDataToPrefab: Boolean = true,
    @YamlComment("Convert blocks to and from geary.")
    val trackBlocks: Boolean = true,
    val logLevel: Severity = Severity.Warn,
)
