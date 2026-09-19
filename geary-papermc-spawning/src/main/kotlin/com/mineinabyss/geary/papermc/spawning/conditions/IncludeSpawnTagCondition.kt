package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.locations.LocationsFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:in_region_with_tags")
class IncludeSpawnTagCondition(
    val tags: List<String>,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        if (tags.isEmpty()) return false
        val unified = gearyPaper.features.get(LocationsFeature).get<SpawnLocationsUnified>()
        val loc = location ?: return false

        if (unified.unified.values.none { hasAllTags(it) && it.isInside(loc) }) return false

        val activeOverride = unified.overrideAt(loc)?.value
        return activeOverride == null || hasAllTags(activeOverride)
    }

    private fun hasAllTags(def: SpawnLocationConfig): Boolean =
        def.tags.isNotEmpty() && tags.all { it in def.tags }
}
