package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.papermc.spawning.SpawningContext
import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.conditions.InRegionsCondition
import com.mineinabyss.geary.papermc.spawning.conditions.IncludeSpawnTagCondition
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.helpers.WeightedList
import org.bukkit.Location

class SpawnChooser(
    val wg: WorldGuardSpawning,
    val caps: MobCaps,
    val context: SpawningContext,
    val spawnLocations: SpawnLocationsUnified,
) {
    private val customRegionSpawns: List<SpawnEntry> by lazy {
        context.spawns
            .map { it.entry }
            .filter { entry -> entry.regions.isEmpty() && entry.regionConditions().any() }
    }

    fun getAllowedSpawnsNear(location: Location, position: SpawnPosition): List<SpawnEntry>? {
        val regions = wg.getRegionsAt(location)
        val wgSpawns = wg.getSpawnsForRegions(regions)
        val customSpawns = customRegionSpawns.filter { it.matchesCustomRegionAt(location) }
        val spawnsInRegion = (wgSpawns + customSpawns).takeUnless { it.isEmpty() } ?: return null

        // a predicate for the filter
        val positionPredicate = { spawn: SpawnEntry -> spawn.position == position }

        // allow MobCaps to directly handle the filter predicate
        return caps.filterAllowedAt(location, spawnsInRegion, positionPredicate)
    }

    fun chooseAllowedSpawnNear(location: Location, position: SpawnPosition): SpawnEntry? {
        val allowedSpawns = getAllowedSpawnsNear(location, position)?.takeUnless { it.isEmpty() } ?: return null
        return WeightedList(allowedSpawns.associateWith { it.priority }).roll()
    }

    private fun SpawnEntry.regionConditions(): Sequence<GearyComponent> {
        return conditions.asSequence()
            .flatMap { it.conditions.asSequence() }
            .filter { it is InRegionsCondition || it is IncludeSpawnTagCondition }
    }

    private fun SpawnEntry.matchesCustomRegionAt(location: Location): Boolean {
        return regionConditions().any { condition ->
            when (condition) {
                is InRegionsCondition -> condition.regions.any {
                    spawnLocations.unified[it]?.isInside(location) == true
                }

                is IncludeSpawnTagCondition -> condition.tags.isNotEmpty() && spawnLocations.unified.values.any { def ->
                    def.tags.isNotEmpty() && condition.tags.all { it in def.tags } && def.isInside(location)
                }

                else -> false
            }
        }
    }
}
