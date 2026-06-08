package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.idofront.config.ConfigEntryWithKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import org.bukkit.Location


@Serializable
// Definition of a single spawn location, a "region" if you may
class SpawnLocationConfig(

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val group: String? = null, // group tag of the location


    //aabb definition of the location
    val locMin:@Serializable(LocationAltSerializer::class) Location,
    val locMax: @Serializable(LocationAltSerializer::class) Location,

    // radius definition of the location (override)
    val center: @Serializable(LocationAltSerializer::class) Location? = null,
    val radius: Int? = null,
    val radiusY: Int? = null,

    val gearySpawnOverride: Boolean = false,

)
{
    fun isInside(location: Location): Boolean {
        if (center != null && radius != null) {
            val dx = location.x - center.x
            val dz = location.z - center.z

            val horizontalDistSq = dx * dx + dz * dz
            val radiusSq = radius * radius

            if (radiusY != null) {
                val dy = kotlin.math.abs(location.y - center.y)
                return horizontalDistSq <= radiusSq && dy <= radiusY
            }

            return horizontalDistSq <= radiusSq
        }

        return location.x >= locMin.x && location.x <= locMax.x &&
                location.y >= locMin.y && location.y <= locMax.y &&
                location.z >= locMin.z && location.z <= locMax.z
    }

    fun getSize() : Double {
        val xSize = locMax.x - locMin.x
        val ySize = locMax.y - locMin.y
        val zSize = locMax.z - locMin.z
        return xSize * ySize * zSize
    }
}


// definition of the spawnable custom locations
@Serializable
class SpawnLocationsConfig(
    val Locations: Map<String, SpawnLocationConfig> = emptyMap(), // id to location
) {

}

class SpawnLocationsUnified(configs: List<ConfigEntryWithKey<SpawnLocationsConfig>>) {
    val unified = mutableMapOf<String, SpawnLocationConfig>()

    init {
        for (config in configs) {
            for ((id, location) in config.entry.Locations) {
                if (unified.containsKey(id)) {
                    println("Warning: Duplicate spawn location ID '$id' found in config '${config.key}'. Skipping this entry.")
                    continue
                }
                unified[id] = location;
            }
        }
    }

}