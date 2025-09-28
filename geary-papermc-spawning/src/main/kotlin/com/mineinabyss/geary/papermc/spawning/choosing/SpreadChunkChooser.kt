package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.random.Random

class SpreadChunkChooser(
    private val mainWorld: World,
    private val db: Database,
    private val dao: SpawnLocationsDAO
) {
    /**
     * Chose a random chunk inside a given bounding box, generally corresponding to a section.
     *
     * Choose the first subsection that has no nearby spawns within a given radius.
     *
     * @param bb The bounding box of the section
     * @param spawner the SpreadSpawner instance
     * @param config the SpreadSpawnConfig containing the algorithm parameters
     * @return a Location representing the chosen chunk, or null if no suitable chunk could be found
     */
    suspend fun chooseChunkInBB(bb: BoundingBox, config: SpreadSpawnConfig, type: String): Location? {
        val radius = config.spreadRadius
        val sectionX = bb.minX.toInt()..bb.maxX.toInt()
        val sectionZ = bb.minZ.toInt()..bb.maxZ.toInt()
        val splitSize = config.splitSize
        val noiseRange = config.spawnNoise * 16
        val sectionCount = db.read { dao.countSpawnsInBBOfType(mainWorld, bb, type) }
        val scoreThreshold = radius * radius
        val sampleSize = (((sectionX.last - sectionX.first) / splitSize) * 0.1)
            .toInt()
            .coerceAtLeast(10)

        // we calculate this here to save a sectionCount query
        if (sectionCount >= config.spawnCap)
            return null

        // generate candidates
        val xRange = (sectionX.first / splitSize)..(sectionX.last / splitSize)
        val zRange = (sectionZ.first / splitSize)..(sectionZ.last / splitSize)
        val chosen = generateSequence { (xRange.random() * splitSize) to (zRange.random() * splitSize) }
            .take(sampleSize)
            .distinct()
            // Choose first subsection with no nearby spawns
            .firstOrNull { (x, z) -> findNearestSq(x, z, type) >= scoreThreshold }
            ?: return null
        val noisyX = (chosen.first + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionX)
        val noisyZ = (chosen.second + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionZ)
        return Location(
            mainWorld,
            noisyX.toDouble(),
            0.0,
            noisyZ.toDouble()
        )
    }

    private suspend fun findNearestSq(x: Int, z: Int, type: String): Double = db.read {
        val loc = Location(mainWorld, x.toDouble(), 0.0, z.toDouble())
        val distance = dao.getClosestSpawnOfType(loc, 1000.0, type)
            ?.location?.distanceSquared(loc)
            ?: Double.MAX_VALUE
        if (distance > 150 * 150) {
            println(" no entity in range for chunk $x, $z: $distance")
        }
        if (distance == Double.MAX_VALUE) {
            println("startloc = $x, $z")
        }
        distance
    }
}




