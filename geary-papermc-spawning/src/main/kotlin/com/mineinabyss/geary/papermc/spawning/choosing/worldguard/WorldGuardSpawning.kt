package com.mineinabyss.geary.papermc.spawning.choosing.worldguard

import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Location

class WorldGuardSpawning(
    val spawns: List<SpawnEntry>,
) {
    val regionToSpawns: Map<String, List<SpawnEntry>> = spawns
        .flatMap { spawnEntry -> spawnEntry.regions.map { region -> region to spawnEntry } }
        .groupBy({ it.first }, { it.second })

    private val regionContainer = WorldGuard.getInstance().platform.regionContainer

    fun getRegionsAt(location: Location): List<ProtectedRegion> {
        val allRegions = regionContainer
            .createQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .regions
            .sortedBy { it.priority }
            // Any regions with the override flag set to true will ignore lower priority spawns
        val dropAt = allRegions
            .indexOfLast { it.getFlag(SpawningWorldGuardFlags.OVERRIDE_LOWER_PRIORITY_SPAWNS) == true }
            .coerceAtLeast(0)
        return allRegions.drop(dropAt)
    }

    fun getSpawnsForRegions(
        regions: List<ProtectedRegion>,
    ): List<SpawnEntry> = regions.flatMapTo(ObjectArrayList()) { regionToSpawns[it.id] ?: emptyList() }
}
