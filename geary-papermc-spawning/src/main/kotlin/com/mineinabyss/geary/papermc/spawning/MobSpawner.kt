package com.mineinabyss.geary.papermc.spawning

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.conditions.IncludeSpawnTagCondition
import com.mineinabyss.geary.papermc.spawning.conditions.InRegionsCondition
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location
import kotlin.random.Random

class MobSpawner(
    val spawnChooser: SpawnChooser,
    val spreadRepo: LocationSpread,
    val spawnLocations: SpawnLocationsUnified,
    val logger: Logger,
) {
    fun checkSpawnConditions(spawn: SpawnEntry, location: Location): Boolean {
        if (!passesOverrideRegions(spawn, location)) return false

        // Check dynamic conditions
        return spawn.conditions.all {
            it.conditionsMet(
                ActionGroupContext().apply {
                    this.location = location.clone()
                    environment["spawnTypes"] = listOf(spawn.type.key)
                }
            )
        }
    }

    private fun passesOverrideRegions(spawn: SpawnEntry, location: Location): Boolean {
        val overrides = spawnLocations.unified.filterValues { it.gearySpawnOverride && it.isInside(location) }
        if (overrides.isEmpty()) return true
        if (overrides.size > 1) logger.w {
            "Multiple override regions (${overrides.keys.joinToString()}) contain location " +
                    "[${location.blockX}, ${location.blockY}, ${location.blockZ}] in ${location.world?.name}, using smallest."
        }
        val (regionId, region) = overrides.entries.minBy { it.value.getSize() }

        return spawn.conditions.any { ensure ->
            ensure.conditions.any { condition ->
                when (condition) {
                    is IncludeSpawnTagCondition ->
                        condition.tags.isNotEmpty() && condition.tags.all { it in region.tags }
                    is InRegionsCondition -> regionId in condition.regions
                    else -> false
                }
            }
        }
    }

    /**
     * Choose and attempt a spawn at a [location] using allowed spawns based on [position].
     *
     * @return Whether the spawn succeeded.
     */
    fun attemptSpawnAt(location: Location, position: SpawnPosition): Boolean {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position) ?: return false

        if (spawn.chance != 1.0 && Random.nextDouble() > spawn.chance) return false
        if (runCatching { !checkSpawnConditions(spawn, location) }.getOrDefault(true)) return false

        repeat(spawn.amount.randomOrMin()) {
            val spread = spawn.spread.randomOrMin().toDouble()
            val ySpread = spawn.ySpread.randomOrMin().toDouble()
            val spawnLoc = if (spread == 0.0 && ySpread == 0.0) location
            else spreadRepo.getNearbySpawnLocation(position, location, spread, ySpread)

            val spawned = spawn.type.spawnAt(spawnLoc)

            val nonSuffocatingLoc = spreadRepo.ensureSuitableLocationOrNull(
                spawnLoc,
                spawned.boundingBox,
                extraAttemptsUp = 10
            ) ?: run {
                spawned.remove()
                return@repeat
            }
            spawned.teleportAsync(nonSuffocatingLoc)
        }
        return true
    }
}
