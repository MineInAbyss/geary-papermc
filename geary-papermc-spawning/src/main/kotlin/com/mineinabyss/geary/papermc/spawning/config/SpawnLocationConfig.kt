package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.idofront.config.ConfigEntryWithKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
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
    private val xRange = minX..maxX
    private val yRange = minY..maxY
    private val zRange = minZ..maxZ

    fun isInside(location: Location): Boolean = isInside(location.x, location.y, location.z)

    fun isInside(x: Double, y: Double, z: Double): Boolean {
        if (center != null && radius != null) {
            val dx = x - center.x
            val dz = z - center.z

            val horizontalDistSq = dx * dx + dz * dz
            val radiusSq = radius * radius

            if (radiusY != null) {
                val dy = kotlin.math.abs(y - center.y)
                return horizontalDistSq <= radiusSq && dy <= radiusY
            }

            return horizontalDistSq <= radiusSq
        }

        return x in xRange && y in yRange && z in zRange
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
class SpawnLocationsConfig(@SerialName("Locations") val locations: Map<String, SpawnLocationConfig> = emptyMap())

class SpawnLocationsUnified(configs: List<ConfigEntryWithKey<SpawnLocationsConfig>>) {
    val unified = mutableMapOf<String, SpawnLocationConfig>()

    init {
        for ((_, key, entry) in configs) for ((id, location) in entry.locations) {
            if (unified.containsKey(id)) {
                println("Warning: Duplicate spawn location ID '$id' found in config '$key'. Skipping this entry.")
                continue
            }
            unified[id] = location
        }
    }

    private val overridesBySize = unified.entries
        .filter { it.value.gearySpawnOverride }
        .sortedBy { it.value.getSize() }

    /** The smallest override region containing [location], which takes precedence over any other spawn rules there. */
    fun overrideAt(location: Location): Map.Entry<String, SpawnLocationConfig>? =
        overridesBySize.firstOrNull { it.value.isInside(location) }
}