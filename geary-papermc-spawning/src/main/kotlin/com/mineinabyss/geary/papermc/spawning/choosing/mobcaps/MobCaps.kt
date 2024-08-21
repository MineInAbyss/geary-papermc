package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadSpawnCategoryEvent
import org.bukkit.Location

class MobCaps(
    val caps: Map<SpawnCategory, Int>,
    val searchRadius: Int,
) {
    fun calculateCategoriesNear(location: Location): Map<SpawnCategory, Int> = location
        .getNearbyEntities(searchRadius.toDouble(), searchRadius.toDouble(), searchRadius.toDouble())
        .groupingBy {
            GearyReadSpawnCategoryEvent(it).also { it.callEvent() }.category ?: SpawnCategory.of(it)
        }
        .eachCount()

    fun getAllowedCategoriesAt(location: Location): List<SpawnCategory> {
        val mobCaps = calculateCategoriesNear(location)
        return caps
            .filter { (category, cap) -> mobCaps.getOrDefault(category, 0) < cap }
            .keys.toList()
    }

//    fun isCategoryAllowedAt(location: Location, category: String): Boolean {
//        val mobCaps = calculateCategoriesNear(location)
//        return mobCaps.getOrDefault(category, 0) < caps.getOrDefault(category, 0)
//    }
}
