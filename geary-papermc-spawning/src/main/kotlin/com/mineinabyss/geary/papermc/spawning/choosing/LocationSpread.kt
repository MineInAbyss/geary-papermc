package com.mineinabyss.geary.papermc.spawning.choosing

import org.bukkit.Location
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sign
import kotlin.random.Random

class LocationSpread {
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


    /**
     * Gets a location to spawn in a mob given an original location and min/max radii around it
     *
     * @param loc    the location to check off of
     * @param maxRad the maximum radius for the new location to be picked at
     * @return a new position to spawn in
     */
    private fun getSpawnInRadius(loc: Location, maxRad: Double): Location? {
        if (maxRad == 0.0) return loc
        if (!loc.chunk.isLoaded) return null
        for (i in 0..29) { //TODO, arbitrary number, should instead search all locations around the spawn
            val x = sign(Math.random() - 0.5) * Random.nextDouble(maxRad)
            val z = sign(Math.random() - 0.5) * Random.nextDouble(maxRad)
            val searchLoc: Location = loc.clone().add(Vector(x, 0.0, z))

            return if (!searchLoc.block.type.isSolid)
                searchLoc.checkDown(2) ?: continue
            else
                searchLoc.checkUp(2) ?: continue
        }
        return null
    }

    private fun Location.checkDown(maxI: Int): Location? {
        var l = clone()
        for (i in 0 until maxI) {
            l = l.add(0.0, -1.0, 0.0)
            if (l.y < l.world.minHeight) return null
            if (l.y >= l.world.maxHeight) l.y = l.world.maxHeight.toDouble()
            if (l.block.type.isSolid) return l.add(0.0, 1.0, 0.0)
        }
        return null
    }

    private fun Location.checkUp(maxI: Int): Location? {
        var l = clone()
        for (i in 0 until maxI) {
            l = l.add(0.0, 1.0, 0.0)
            if (!l.block.type.isSolid) {
                return l
            }
            if (l.y >= l.world.maxHeight) return null
            if (l.y < l.world.minHeight) l.y = 10.0
        }
        return null
    }

}
