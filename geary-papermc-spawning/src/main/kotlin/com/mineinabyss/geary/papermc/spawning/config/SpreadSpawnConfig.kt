package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SpreadSpawnConfig(
    @YamlComment("Entity entry, e.g. 'mm:prayingskeleton'")
    val type: String,
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
    val SpawnDelay: Int = 40,
    val WorldName: String = "world",
    val sectionsConfig: Map<String, SpreadSpawnConfig> = emptyMap()
)