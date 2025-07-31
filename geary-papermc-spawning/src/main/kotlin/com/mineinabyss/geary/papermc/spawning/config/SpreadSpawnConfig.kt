package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SpreadSpawnConfig(
    @YamlComment("Entity type, e.g. 'mm:prayingskeleton'")
    val entityType: String,
    @YamlComment("How far the entity should be from another entity of the same type")
    val spreadRadius: Int,
    val spawnCap: Int,
    //val spawnTickDelay: Int, // can't really change that since its handled by the task
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
    @YamlComment("Radius around the selected block to be valid")
    val openAreaWidth: Int,
    @YamlComment("Height of the open area around the selected block to be valid")
    val openAreaHeight: Int,
) {

}

@Serializable
@SerialName("geary:spread_spawn_sections")
data class SpreadSpawnSectionsConfig(
    val sectionsConfig: Map<String, SpreadSpawnConfig> = emptyMap()
)