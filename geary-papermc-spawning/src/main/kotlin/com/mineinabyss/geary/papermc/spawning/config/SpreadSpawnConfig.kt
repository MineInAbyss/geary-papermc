package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
class SpreadSpawnConfig(
    @YamlComment("Entity spawn entry, e.g. 'type: mm:prayingskeleton'")
    val entry: SpawnEntry,
    @YamlComment("How far the entity should be from another entity of the same type")
    val spreadRadius: Int,
    val spawnCap: Int,
    @YamlComment("Lowest point of the section to spawn in")
    val sectionMinY: Int,
    @YamlComment("Highest point of the section to spawn in")
    val sectionMaxY: Int,
    @YamlComment("How many chunks the selected area should deviate from the chosen area at most")
    val spawnNoise: Int,
    @YamlComment("The size of each 'split' we do when spawning entities, higher means more performance but less precision")
    val splitSize: Int,
    @YamlComment("How many times to try and spawn an entity in a chunk before giving up")
    val spawnAttempts: Int,
)

@Serializable
@SerialName("geary:spread_spawn_sections")
data class SpreadSpawnSectionsConfig(
    val spawnDelay: Int = 40,
    val worldName: String = "world",
    @Serializable(with = DurationSerializer::class)
    val clearSpawnsOlderThan: Duration = 7.days,
    val sectionsConfig: Map<String, SpreadSpawnConfig> = emptyMap(),
)
