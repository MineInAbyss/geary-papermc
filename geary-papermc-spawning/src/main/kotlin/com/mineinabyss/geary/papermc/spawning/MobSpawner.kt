package com.mineinabyss.geary.papermc.spawning

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.NearbyMobs
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

    /** [nearby] lets conditions like maxNearby count from an existing scan instead of scanning the world */
    fun checkSpawnConditions(spawn: SpawnEntry, location: Location, nearby: NearbyMobs? = null): Boolean {
        if (!passesOverrideRegions(spawn, location)) return false

        // Conditions are run directly since EnsureAction signals failure with an exception that fills in its stack trace.
        // Its other components only emit to the context entity, which spawn checks never have
        return spawn.conditions.all { ensure ->
            val context = ActionGroupContext().apply {
                this.location = location.clone()
                environment["spawnTypes"] = listOf(spawn.type.key)
                if (nearby != null) environment[NearbyMobs.ENVIRONMENT_KEY] = nearby
            }
            ensure.conditions.all { condition ->
                condition !is Condition || with(condition) { context.execute() }
            }
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
     * Spawned mobs are recorded in [nearby] so it stays valid across attempts sharing it.
     *
     * @return Whether the spawn succeeded.
     */
    fun attemptSpawnAt(
        location: Location,
        position: SpawnPosition,
        nearby: Lazy<NearbyMobs>,
    ): Boolean {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position, nearby) ?: return false

        if (spawn.chance != 1.0 && Random.nextDouble() > spawn.chance) return false
        if (!conditionsPassOrReport(spawn, location, nearby.value)) return false

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
                nearby.value.add(spawn.type.key, spawn.type.category, suitableLoc)
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
            nearby.value.add(spawn.type.key, spawn.type.category, suitableLoc)
            spawnedCount++
        }
        return spawnedCount > 0
    }

    private fun conditionsPassOrReport(spawn: SpawnEntry, location: Location, nearby: NearbyMobs): Boolean =
        runCatching { checkSpawnConditions(spawn, location, nearby) }.getOrElse { e ->
            if (reportedConditionFailures.add(spawn.type.key)) logger.w {
                "Conditions for spawn ${spawn.type.key} threw and will block it until fixed: ${e.stackTraceToString()}"
            }
            false
        }
}
