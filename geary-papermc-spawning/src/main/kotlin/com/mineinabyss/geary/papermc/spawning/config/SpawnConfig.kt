package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * @property spawnChunksAroundPlayer the minimum number of chunks away from the player in which a mob can spawn
 * @property playerGroupRadius the radius around which players will count mobs towards the local mob cap
 * @property spawnTaskDelay the delay in ticks between each attempted mob spawn
 * @property creatureTypeCaps Per-player mob caps for spawning of [NMSCreatureType]s on the server.
 * @property spawnHeightRange The maximum amount above or below players that mobs can spawn.
 */

@Serializable
data class SpawnConfig(
    val playerCaps: Map<SpawnCategory, Int> = mapOf(
        SpawnCategory("flying") to 10,
        SpawnCategory("hostile") to 70,
        SpawnCategory("passive") to 10,
        SpawnCategory("water") to 10,
    ),
    val globalCaps: Map<String, Int> = mapOf(),
    val range: Range = Range(),
    val runTimes: Map<SpawnPosition, Duration> = mapOf(),
    val maxSpawnAttemptsPerPlayer: Int = 10,
) {
    @Serializable
    data class Range(
        val maxDistance: Int = 64,
        val minDistance: Int = 16,
        val maxVerticalDistance: Int = 32,
        val playerCapRadius: Int = 128,
    )
}
