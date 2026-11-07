package com.mineinabyss.geary.papermc.spawning.choosing.regions

import com.mineinabyss.geary.papermc.regions.GearyRegions
import com.mineinabyss.geary.papermc.spawning.SpawningContext
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import org.bukkit.Location

class RegionSpawning(
    context: SpawningContext,
    private val regions: GearyRegions,
) {
    val regionToSpawns: Map<String, List<SpawnEntry>> = context.spawns
        .map { it.entry }
        .flatMap { spawnEntry -> spawnEntry.regions.map { region -> region to spawnEntry } }
        .groupBy({ it.first }, { it.second })

    fun getSpawnsAt(location: Location): List<SpawnEntry> =
        regions.spawnRegionsAt(location).flatMap { regionToSpawns[it] ?: emptyList() }
}
