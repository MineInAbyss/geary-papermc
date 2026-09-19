package com.mineinabyss.geary.papermc.spawning

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.conditions.IncludeSpawnTagCondition
import com.mineinabyss.geary.papermc.spawning.conditions.InRegionsCondition
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class MobSpawner(
    val spawnChooser: SpawnChooser,
    val spreadRepo: LocationSpread,
    val spawnLocations: SpawnLocationsUnified,
    val logger: Logger,
) {
    private val reportedConditionFailures = ConcurrentHashMap.newKeySet<String>()

    fun checkSpawnConditions(spawn: SpawnEntry, location: Location): Boolean {
        if (!passesOverrideRegions(spawn, location)) return false

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
        val (regionId, region) = spawnLocations.overrideAt(location) ?: return true

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
     * [categoryCounts] is bumped by what gets spawned so it stays valid across attempts sharing it.
     *
     * @return Whether the spawn succeeded.
     */
    fun attemptSpawnAt(
        location: Location,
        position: SpawnPosition,
        categoryCounts: Lazy<MutableMap<SpawnCategory, Int>>,
    ): Boolean {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position, categoryCounts) ?: return false

        if (spawn.chance != 1.0 && Random.nextDouble() > spawn.chance) return false
        if (!conditionsPassOrReport(spawn, location)) return false

        var spawnedCount = 0
        repeat(spawn.amount.randomOrMin()) {
            val spread = spawn.spread.randomOrMin().toDouble()
            val ySpread = spawn.ySpread.randomOrMin().toDouble()
            val spawnLoc = if (spread == 0.0 && ySpread == 0.0) location
            else spreadRepo.getNearbySpawnLocation(position, location, spread, ySpread)

            val expectedBox = spawn.type.boundingBoxAt(spawnLoc)
            if (expectedBox != null) {
                val suitableLoc = spreadRepo.ensureSuitableLocationOrNull(spawnLoc, expectedBox, extraAttemptsUp = 10)
                    ?: return@repeat
                spawn.type.spawnAt(suitableLoc)
                spawnedCount++
                return@repeat
            }

            // Unknown dimensions, so spawn first and correct afterwards
            val spawned = spawn.type.spawnAt(spawnLoc)
            val suitableLoc = spreadRepo.ensureSuitableLocationOrNull(spawnLoc, spawned.boundingBox, extraAttemptsUp = 10)
                ?: run {
                    spawned.remove()
                    return@repeat
                }
            if (suitableLoc != spawnLoc) spawned.teleport(suitableLoc)
            spawnedCount++
        }
        if (spawnedCount > 0) categoryCounts.value.merge(spawn.type.category, spawnedCount, Int::plus)
        return spawnedCount > 0
    }

    private fun conditionsPassOrReport(spawn: SpawnEntry, location: Location): Boolean =
        runCatching { checkSpawnConditions(spawn, location) }.getOrElse { e ->
            if (reportedConditionFailures.add(spawn.type.key)) logger.w {
                "Conditions for spawn ${spawn.type.key} threw and will block it until fixed: ${e.stackTraceToString()}"
            }
            false
        }
}
