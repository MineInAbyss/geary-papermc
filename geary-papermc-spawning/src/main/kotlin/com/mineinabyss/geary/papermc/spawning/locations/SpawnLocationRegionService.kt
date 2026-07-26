package com.mineinabyss.geary.papermc.spawning.locations

import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified

import org.bukkit.Location

class SpawnLocationRegionService(
    private val locations: SpawnLocationsUnified,
) : RegionService {
    override val regionIds: Set<String> get() = locations.unified.keys

    override fun regionsAt(location: Location): Set<String> =
        locations.unified.filterValues { it.isInside(location) }.keys

    override fun isInside(regionId: String, location: Location): Boolean =
        locations.unified[regionId]?.isInside(location) == true
}
