package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.geary.papermc.spawning.readers.SpawnPositionReader
import org.bukkit.Location
import org.bukkit.util.BoundingBox
import kotlin.math.ceil
import kotlin.math.floor

class LocationSpread(
    val spawnPositionReader: SpawnPositionReader,
    val triesForNearbyLoc: Int,
) {
    val random = java.util.Random()

    /**
     * Ensures that the given location is suitable for spawning an entity by checking if it is inside any solid blocks.
     * If it is, it will attempt to find a suitable location by shifting the bounding box upwards.
     *
     * @param chosenLoc The chosen location for spawning the entity.
     * @param bb The bounding box of the entity.
     * @param extraAttemptsUp The number of additional attempts to find a suitable location when inside a block. Defaults to the value specified in the mobzySpawning config.
     * @return A suitable location for spawning the entity, or null if no suitable location is found.
     */
    fun ensureSuitableLocationOrNull(
        chosenLoc: Location,
        bb: BoundingBox,
        extraAttemptsUp: Int,
    ): Location? {
        val bb = bb.clone()
        // We shrink the box by a bit since overlap checks are strict inequalities
        val bbShrunk = bb.clone().apply {
            expand(-0.1, -0.1, -0.1, -0.1, -0.1, -0.1)
        }

        repeat(extraAttemptsUp + 1) { offsetY ->
            checkLoop@ for (x in floor(bb.minX).toInt()..ceil(bb.maxX).toInt())
                for (y in floor(bb.minY).toInt()..ceil(bb.maxY).toInt())
                    for (z in floor(bb.minZ).toInt()..ceil(bb.maxZ).toInt())
                        if (chosenLoc.world.getBlockAt(x, y, z).collisionShape.boundingBoxes.any { shape ->
                                shape.shift(x.toDouble(), y.toDouble(), z.toDouble())
                                shape.overlaps(bbShrunk)
                            }) {
                            bb.shift(0.0, 1.0, 0.0)
                            bbShrunk.shift(0.0, 1.0, 0.0)
                            return@repeat
                        }
            return chosenLoc.clone().add(0.0, offsetY.toDouble(), 0.0)
        }
        return null
    }

    fun getNearbySpawnLocation(
        position: SpawnPosition,
        loc: Location,
        horizontalRange: Double,
        verticalRange: Double,
    ): Location {
        // generate x, z offsets within the radius using a normal distribution
        repeat(triesForNearbyLoc) {
            val dx = random.nextGaussian(0.0, horizontalRange).toInt()
            val dz = random.nextGaussian(0.0, horizontalRange).toInt()
            val dy = verticalRange * random.nextDouble(0.0, 1.0).toInt()
            val offsetLoc = loc.clone().apply { add(dx.toDouble(), dy.toDouble(), dz.toDouble()) }
            if (spawnPositionReader.spawnPositionFor(offsetLoc) == position) return offsetLoc
        }
        return loc
    }
}
