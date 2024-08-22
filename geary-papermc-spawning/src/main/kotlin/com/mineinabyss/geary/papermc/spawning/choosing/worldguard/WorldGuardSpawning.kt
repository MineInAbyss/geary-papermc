package com.mineinabyss.geary.papermc.spawning.choosing.worldguard

import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Location

class WorldGuardSpawning(
    val spawns: List<SpawnEntry>,
) {
    val regionToSpawns: Map<String, List<SpawnEntry>> = spawns
        .flatMap { spawnEntry -> spawnEntry.regions.map { region -> region to spawnEntry } }
        .groupBy({ it.first }, { it.second })

    private val regionContainer = WorldGuard.getInstance().platform.regionContainer

    fun getRegionsAt(location: Location): List<ProtectedRegion> {
        return regionContainer
            .createQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .regions
            .sorted()
    }

    fun getSpawnsForRegions(
        regions: List<ProtectedRegion>,
    ): List<SpawnEntry> = regions.flatMap { regionToSpawns[it.id] ?: emptyList() }
}