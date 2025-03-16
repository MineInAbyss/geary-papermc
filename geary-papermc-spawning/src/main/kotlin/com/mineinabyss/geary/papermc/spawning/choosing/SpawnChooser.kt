package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.choosing.mobcaps.MobCaps
import com.mineinabyss.geary.papermc.spawning.choosing.worldguard.WorldGuardSpawning
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.helpers.WeightedList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Location

class SpawnChooser(
    val wg: WorldGuardSpawning,
    val caps: MobCaps,
) {
    fun getAllowedSpawnsNear(
        location: Location,
        position: SpawnPosition,
    ): List<SpawnEntry>? {
        // Get spawns from regions
        val regions = wg.getRegionsAt(location)
        val spawnsInRegion = wg.getSpawnsForRegions(regions).takeUnless { it.isEmpty() } ?: return null

        // Check mob caps
        val allowedSpawns = caps.filterAllowedAt(location, spawnsInRegion.filterTo(ObjectArrayList()) { it.position == position })

        return allowedSpawns
    }

    fun chooseAllowedSpawnNear(location: Location, position: SpawnPosition): SpawnEntry? {
        val allowedSpawns = getAllowedSpawnsNear(location, position)?.takeUnless { it.isEmpty() } ?: return null
        return WeightedList(allowedSpawns.associateWithTo(Object2ObjectOpenHashMap()) { it.priority }).roll()
    }
}
