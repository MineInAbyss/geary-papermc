package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * This is a javadoc comment
 */
@Serializable
data class SpawnConfig(
    @YamlComment("Default per player spawn cap limit when not explicitly set in playerCaps")
    val defaultCap: Int = 10,
    @YamlComment("The maximum number of mobs around each palyer per spawn category.")
    val playerCaps: Map<SpawnCategory, Int> = mapOf(
        SpawnCategory("flying") to 10,
        SpawnCategory("hostile") to 70,
        SpawnCategory("passive") to 10,
        SpawnCategory("water") to 10,
    ),
    @YamlComment("Options about the spawn range of mobs around each player")
    val range: Range = Range(),
    @YamlComment("The delay between each spawning-task run")
    val spawnDelay: @Serializable(DurationSerializer::class) Duration = 1.ticks,
    @YamlComment("How often should each spawn category, attempt spawns. The higher a value, the more time between spawn attempts.")
    val runTimes: Map<SpawnPosition, @Serializable(with = DurationSerializer::class) Duration> = mapOf(),
    @YamlComment("How many times to try and find a valid spawn position around a player before giving up.")
    val maxSpawnAttemptsPerPlayer: Int = 10,
) {
    @Serializable
    data class Range(
        val maxDistance: Int = 64,
        val minDistance: Int = 16,
        val maxVerticalDistance: Int = 32,
        @YamlComment("Radius around the player to count mob categories for spawn caps.")
        val playerCapRadius: Int = 128,
        @YamlComment("Radius to count nearby mobs for the maxNearby condition.")
        val defaultNearbyRange: Double = 128.0,
    )
}
