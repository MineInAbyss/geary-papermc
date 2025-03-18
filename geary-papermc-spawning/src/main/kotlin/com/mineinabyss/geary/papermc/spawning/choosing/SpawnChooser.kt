package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.helpers.WeightedList
import org.bukkit.Location

class SpawnChooser(
    val wg: WorldGuardSpawning,
    val caps: MobCaps,
) {
    fun getAllowedSpawnsNear(location: Location, position: SpawnPosition): List<SpawnEntry>? {
        val regions = wg.getRegionsAt(location)
        val spawnsInRegion = wg.getSpawnsForRegions(regions).takeUnless { it.isEmpty() } ?: return null

        // a predicate for the filter
        val positionPredicate = { spawn: SpawnEntry -> spawn.position == position }

        // allow MobCaps to directly handle the filter predicate
        return caps.filterAllowedAt(location, spawnsInRegion, positionPredicate)
    }

    fun chooseAllowedSpawnNear(location: Location, position: SpawnPosition): SpawnEntry? {
        val allowedSpawns = getAllowedSpawnsNear(location, position)?.takeUnless { it.isEmpty() } ?: return null
        return WeightedList(allowedSpawns.associateWith { it.priority }).roll()
    }
}
