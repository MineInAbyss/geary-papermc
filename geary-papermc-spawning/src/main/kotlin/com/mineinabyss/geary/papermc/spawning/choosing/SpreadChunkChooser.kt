package com.mineinabyss.geary.papermc.spawning.choosing

import co.touchlab.kermit.Logger
import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class SpreadChunkChooser(
    private val logger: Logger,
    private val mainWorld: World,
    private val spawnLocs: SpreadSpawnRepository,
) {
    // Cache to prevent re-checking full sections as frequently. Unit is placed to mark a section as full.
    // Keys are bounding box to type pairs. An entry being present means this section and type were recently full.
    private val fullSectionCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10.seconds.toJavaDuration())
        .build<Pair<BoundingBox, String>, Unit>()

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

        // Get count in section, checking cache first to see if the section was recently filled.
        // If so, wait a little before re-executing DB call
        if (fullSectionCache.getIfPresent(bb to type) != null) return null
        val sectionCount = spawnLocs.countSpawnsInBB(mainWorld, bb, type)
        if (sectionCount >= config.spawnCap) {
            fullSectionCache.put(bb to type, Unit)
            return null
        }

        val scoreThreshold = radius * radius
        val sampleSize = (((sectionX.last - sectionX.first) / splitSize) * 0.1)
            .toInt()
            .coerceAtLeast(10)

        val xRange = (sectionX.first / splitSize)..(sectionX.last / splitSize)
        val zRange = (sectionZ.first / splitSize)..(sectionZ.last / splitSize)
        val chosen = generateSequence { (xRange.random() * splitSize) to (zRange.random() * splitSize) }
            .take(sampleSize)
            .distinct()
            .firstOrNull { (x, z) ->
                val dist = findNearestSq(x, z, type)
                dist >= scoreThreshold
            }

        if (chosen == null) {
            return null
        }
        logger.v { "Checking at ${chosen.first}, ${chosen.second}" }
        val noisyX = (chosen.first + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionX)
        val noisyZ = (chosen.second + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionZ)
        return Location(mainWorld, noisyX.toDouble(), 0.0, noisyZ.toDouble())
    }

    private suspend fun findNearestSq(x: Int, z: Int, type: String): Double = TODO() /*db.read {
        val loc = Location(mainWorld, x.toDouble(), 0.0, z.toDouble())
        dao.getClosestSpawnOfType(loc, 1000.0, type)
            ?.location?.distanceSquared(loc)
            ?: Double.MAX_VALUE
    }*/
}


