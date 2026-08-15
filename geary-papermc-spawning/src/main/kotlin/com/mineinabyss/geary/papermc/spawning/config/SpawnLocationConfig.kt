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
    val tags: List<String> = emptyList(),


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
    private val minX = minOf(locMin.x, locMax.x)
    private val maxX = maxOf(locMin.x, locMax.x)
    private val minY = minOf(locMin.y, locMax.y)
    private val maxY = maxOf(locMin.y, locMax.y)
    private val minZ = minOf(locMin.z, locMax.z)
    private val maxZ = maxOf(locMin.z, locMax.z)

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

        return location.x >= minX && location.x <= maxX &&
                location.y >= minY && location.y <= maxY &&
                location.z >= minZ && location.z <= maxZ
    }

    fun getSize(): Double {
        val xSize = maxX - minX
        val ySize = maxY - minY
        val zSize = maxZ - minZ
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