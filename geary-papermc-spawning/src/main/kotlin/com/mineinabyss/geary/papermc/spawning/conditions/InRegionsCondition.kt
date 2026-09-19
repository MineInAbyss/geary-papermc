package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.papermc.spawning.locations.LocationsFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:in_regions")
class InRegionsCondition(
    val regions: List<String>,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val unified = gearyPaper.features.get(LocationsFeature).get<SpawnLocationsUnified>()
        val loc = location ?: return false

        if (regions.none { unified.unified[it]?.isInside(loc) == true }) return false

        val activeOverride = unified.overrideAt(loc)?.key
        return activeOverride == null || activeOverride in regions
    }
}
