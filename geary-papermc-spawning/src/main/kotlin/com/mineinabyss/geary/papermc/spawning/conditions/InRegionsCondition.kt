package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.DIScope
import com.mineinabyss.dependencies.get
import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsConfig
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified

@Serializable
@SerialName("geary:in_regions")
class InRegionsCondition(
    val regions: List<String>,
): Condition
{
     override fun ActionGroupContext.execute(): Boolean {

         val unified = gearyPaper.features.get(SpawningFeature)?.get<SpawnLocationsUnified>() ?: return false
         val config = unified.unified;
         val loc = location ?: return false;


//         return regions.any { testedRegion ->
//             config.Locations[testedRegion]?.isInside(loc) == true
//         }
         val validRegions: MutableList<SpawnLocationConfig> = mutableListOf()
         for (testedRegion in regions) {
             val regionDef = config[testedRegion] ?: continue
             if (regionDef.isInside(loc)) {
                 validRegions.add(regionDef)
             }
         }
         if (validRegions.isEmpty())
             return false
         // now we want to make sure that we're not inside a region that has an override

         // find the highest-priority (smallest) override region that contains this location
         // note: in case of a tie, it's technically UB, in practice, it's gonna be the first one which the config has
         // which in itself, depends on the order each file gets parsed (file location)
         val activeOverride = config.values
             .filter { it.gearySpawnOverride && it.isInside(loc) }
             .minByOrNull { it.getSize() }

         // if the current location is inside a location that has an override but isn't in our list of regions
         // then, the current location is inside a region that would override our spawn, so we should not spawn here
         if (activeOverride != null && activeOverride !in validRegions) {
             return false
         }

         return true
    }
}