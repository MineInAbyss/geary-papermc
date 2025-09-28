package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.random.Random
import kotlin.ranges.rangeTo
import kotlin.text.compareTo
import kotlin.text.toInt
import kotlin.times
import kotlin.unaryMinus

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
            val timings = mutableListOf<String>()
            val startTotal = System.currentTimeMillis()

            val radius = config.spreadRadius
            val sectionX = bb.minX.toInt()..bb.maxX.toInt()
            val sectionZ = bb.minZ.toInt()..bb.maxZ.toInt()
            val splitSize = config.splitSize
            val noiseRange = config.spawnNoise * 16

            val sectionCountStart = System.currentTimeMillis()
            val sectionCount = db.read { dao.countSpawnsInBBOfType(mainWorld, bb, type) }
            timings += "sectionCount = $sectionCount (took ${System.currentTimeMillis() - sectionCountStart}ms)"

            val scoreThreshold = radius * radius
            val sampleSize = (((sectionX.last - sectionX.first) / splitSize) * 0.1)
                .toInt()
                .coerceAtLeast(10)
            timings += "scoreThreshold = $scoreThreshold, sampleSize = $sampleSize"

            if (sectionCount >= config.spawnCap) {
                timings += "sectionCount >= spawnCap (${config.spawnCap}), returning null"
                printTimings(timings, startTotal)
                return null
            }

            val candidateStart = System.currentTimeMillis()
            val xRange = (sectionX.first / splitSize)..(sectionX.last / splitSize)
            val zRange = (sectionZ.first / splitSize)..(sectionZ.last / splitSize)
            var checkedCandidates = 0
            val chosen = generateSequence { (xRange.random() * splitSize) to (zRange.random() * splitSize) }
                .take(sampleSize)
                .distinct()
                .onEach { checkedCandidates++ }
                .firstOrNull { (x, z) ->
                    val t = System.currentTimeMillis()
                    val dist = findNearestSq(x, z, type)
                    //timings += "Checked candidate ($x, $z): nearestSq = $dist (took ${System.currentTimeMillis() - t}ms)"
                    dist >= scoreThreshold
                }
            timings += "Candidate selection took ${System.currentTimeMillis() - candidateStart}ms, checked $checkedCandidates candidates"

            if (chosen == null) {
                timings += "No suitable chunk found, returning null"
                printTimings(timings, startTotal)
                return null
            }

            val noisyX = (chosen.first + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionX)
            val noisyZ = (chosen.second + Random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionZ)
            timings += "Chosen chunk: (${chosen.first}, ${chosen.second}), noisy: ($noisyX, $noisyZ)"

            printTimings(timings, startTotal)
            return Location(mainWorld, noisyX.toDouble(), 0.0, noisyZ.toDouble())
        }

        private suspend fun findNearestSq(x: Int, z: Int, type: String): Double = db.read {
            val loc = Location(mainWorld, x.toDouble(), 0.0, z.toDouble())
            dao.getClosestSpawnOfType(loc, 1000.0, type)
                ?.location?.distanceSquared(loc)
                ?: Double.MAX_VALUE
        }

        private fun printTimings(timings: List<String>, startTotal: Long) {
            println("==== SpreadChunkChooser timings ====")
            timings.forEach { println(it) }
            println("Total duration: ${System.currentTimeMillis() - startTotal}ms")
            println("===================================")
        }
    }


