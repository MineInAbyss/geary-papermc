package com.mineinabyss.geary.papermc.spawning.choosing.regions

import com.mineinabyss.geary.papermc.regions.GearyRegions
import com.mineinabyss.geary.papermc.spawning.SpawningContext
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import org.bukkit.Location
import org.bukkit.World

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

    /** Regions with spawns that [getSpawnsAt] could use for some block in the inclusive box, null when the provider cannot tell */
    fun spawnRegionsOverlapping(world: World, minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int): List<String>? =
        regions.spawnRegionsOverlapping(world, minX, minY, minZ, maxX, maxY, maxZ)
            ?.filter { it in regionToSpawns }
            ?.sorted()
}
