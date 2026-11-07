package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import org.bukkit.Location

/**
 * Cap counts around one location, read from a shared [WorldMobs] snapshot.
 * Spawn attempts read caps and nearby counts from it instead of scanning the world again
 */
class NearbyMobs(
    private val snapshot: WorldMobs,
    val categoryCounts: HashMap<SpawnCategory, Int>,
) {
    /** Also records into the world snapshot, so later players in the same world see this spawn before the next refresh */
    fun add(type: String?, category: SpawnCategory, x: Double, y: Double, z: Double) {
        categoryCounts.merge(category, 1, Int::plus)
        snapshot.add(type, category, x, y, z)
    }

    fun add(type: String?, category: SpawnCategory, location: Location) =
        add(type, category, location.x, location.y, location.z)

    /**
     * Counts mobs of [types] within a cube of [radius] around [location], stopping early at [stopAt].
     * Returns null when [location] is in another world than the snapshot, so the caller must scan the world itself
     */
    fun countNear(types: Collection<String>, location: Location, radius: Double, stopAt: Int): Int? {
        if (location.world?.uid != snapshot.world) return null
        return snapshot.countNear(types, location.x, location.y, location.z, radius, stopAt)
    }

    companion object {
        const val ENVIRONMENT_KEY = "nearbyMobs"
    }
}
