package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
class SpreadSpawnConfig(
    @YamlComment("Entity spawn entry, e.g. 'type: mm:prayingskeleton'")
    val entry: SpawnEntry,
    @YamlComment("How far the entity should be from another entity of the same type", "Distance is 3D, with the vertical part scaled by verticalWeight")
    val spreadRadius: Int,
    val spawnCap: Int,
    @YamlComment("Lowest point of the section to spawn in")
    val sectionMinY: Int,
    @YamlComment("Highest point of the section to spawn in")
    val sectionMaxY: Int,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val altSpawnChance: Double = 0.0,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val altSpawnEntry: SpawnEntry = entry,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val blacklistRegions: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @YamlComment("How many random surface positions to draw per attempt, higher finds a spaced spot sooner in crowded sections")
    val candidateCount: Int = 24,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @YamlComment("Scale of vertical distance in spreadRadius checks, 0 ignores height entirely")
    val verticalWeight: Double = 1.0,
) {
    init {
        require(spreadRadius >= 0) { "spreadRadius must not be negative" }
        require(candidateCount > 0) { "candidateCount must be positive" }
        require(verticalWeight >= 0) { "verticalWeight must not be negative" }
        require(sectionMinY <= sectionMaxY) { "sectionMinY must not be above sectionMaxY" }
    }

    fun chooseEntry(enableAltSpawns: Boolean, random: Random = Random): SpawnEntry =
        if (enableAltSpawns && altSpawnChance > 0 && random.nextDouble() * 100 < altSpawnChance) altSpawnEntry
        else entry

    fun entryForType(typeKey: String): SpawnEntry? = when (typeKey) {
        entry.type.key -> entry
        altSpawnEntry.type.key -> altSpawnEntry
        else -> null
    }
}

@Serializable
data class SpreadSpawnSectionsConfig(
    val enableAltSpawns: Boolean = false,
    val worldName: String = "world",
    val sectionsConfig: Map<String, SpreadSpawnConfig> = emptyMap(),
)

@Serializable
@SerialName("geary:spread_entities_types")
data class SpreadEntityTypesConfig(
    val types: Map<String, SpreadSpawnSectionsConfig> = emptyMap(),
    val spawnDelay: Long = 40,
    val worldName: String = "world",
    @Serializable(with = DurationSerializer::class)
    val clearSpawnsOlderThan: Duration = 7.days,
    @YamlComment("Spread mobs never spawn closer than this to a player")
    val minPlayerDistance: Double = 24.0,
    @YamlComment("Spread mobs only spawn once a player is this close horizontally, keep it near entity-tracking-range since players cannot see mobs further out")
    val spawnDistance: Double = 96.0,
    @YamlComment("How many spawnable positions the surface index keeps per chunk")
    val surfaceSamplesPerChunk: Int = 32,
    @Serializable(with = DurationSerializer::class)
    @YamlComment("How old a chunk's surface scan can get before it is rescanned")
    val surfaceRescanAfter: Duration = 3.days,
    @YamlComment("How many unloaded chunks per second are read from disk to index whole sections, 0 only indexes chunks players load")
    val savedChunkScansPerSecond: Int = 20,
) {
    init {
        require(minPlayerDistance >= 0) { "minPlayerDistance must not be negative" }
        require(spawnDistance >= 0) { "spawnDistance must not be negative" }
        require(surfaceSamplesPerChunk > 0) { "surfaceSamplesPerChunk must be positive" }
        require(savedChunkScansPerSecond >= 0) { "savedChunkScansPerSecond must not be negative" }
    }
}