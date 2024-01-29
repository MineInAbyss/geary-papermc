package com.mineinabyss.geary.papermc

import co.touchlab.kermit.Severity
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
class GearyPaperConfig(
    @YamlComment("Convert bukkit entities to and from geary, for instance to store and persist components on a player.")
    val trackEntities: Boolean = true,
    @YamlComment("Convert items to and from geary. Depends on entity tracking.")
    val trackItems: Boolean = true,
    @YamlComment("Convert blocks to and from geary.")
    val trackBlocks: Boolean = true,
    @YamlComment("Convert bukkit events to data in Geary (deprecated)")
    val bridgeEvents: Boolean = true,
    @YamlComment("If an item has no prefabs encoded, try to find its prefab by matching custom model data.")
    val migrateItemCustomModelDataToPrefab: Boolean = true,
    @YamlComment("Whether to throw an error when an entity read operation occurs outside of the server thread.")
    val catchAsyncRead: Boolean = false,
    @YamlComment("Whether to throw an error when an entity write operation occurs outside of the server thread.")
    val catchAsyncWrite: Boolean = true,
    @YamlComment("Whether to throw an error when converting bukkit concepts to geary entities outside of the server thread.")
    val catchAsyncEntityConversion: Boolean = false,
    val catchAsyncArchetypeProviderAccess: Boolean = false,
    val logLevel: Severity = Severity.Warn,
)
