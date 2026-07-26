package com.mineinabyss.geary.papermc.spawning.locations

import org.bukkit.Location

/**
 * Exposes the named world regions defined in Geary's `locations` config folder to other plugins.
 *
 * Registered via Bukkit's services API so consumers only need geary-papermc on their compile classpath:
 *
 * ```kotlin
 * Services.getOrNull<RegionService>()?.regionsAt(player.location)
 * ```
 *
 * [PlayerEnterRegionEvent] and [PlayerExitRegionEvent] fire as players move between regions,
 * so consumers can react to transitions instead of polling.
 */
interface RegionService {
    /** Ids of all known regions. */
    val regionIds: Set<String>

    /** Ids of every region containing [location]. */
    fun regionsAt(location: Location): Set<String>

    /** Whether the region with [regionId] contains [location], false if the region doesn't exist. */
    fun isInside(regionId: String, location: Location): Boolean
}
