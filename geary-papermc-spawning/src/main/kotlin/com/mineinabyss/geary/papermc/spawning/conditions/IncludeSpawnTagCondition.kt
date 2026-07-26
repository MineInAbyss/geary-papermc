package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:in_region_with_tags")
class IncludeSpawnTagCondition(
    val tags: List<String>,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        if (tags.isEmpty()) return false
        val unified = gearyPaper.features.get(SpawningFeature).get<SpawnLocationsUnified>()
        val config = unified.unified
        val loc = location ?: return false

        if (config.values.none { hasAllTags(it) && it.isInside(loc)}) return false

        var activeOverride: SpawnLocationConfig? = null
        var smallestSize = Double.MAX_VALUE
        for (def in config.values) {
            if (!def.gearySpawnOverride || !def.isInside(loc)) continue
            val size = def.getSize()
            if (size < smallestSize) {
                smallestSize = size
                activeOverride = def
            }
        }

        return activeOverride == null || hasAllTags(activeOverride)
    }

    private fun hasAllTags(def: SpawnLocationConfig): Boolean {
        return !(def.tags.isEmpty() || tags.any { it in def.tags })
    }

}