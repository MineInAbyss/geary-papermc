package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawner
import me.dvyy.sqlite.Database
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import kotlin.random.Random

class SpreadChunkChooser {
    /**
     * Chose a random chunk inside a given bounding box, generally corresponding to a section.
     *
     * We start by dividing the section into a configurable amount of sub-sections (defined by config.splitSize).
     * We then select a sample of theses sub-sections at random to avoid unnecessary calculations, theses become candidates.
     * For each candidate, we calculate how isolated they are from existing spread entities.
     * If the distance to the nearest entity is greater than the configured spread radius, we consider it a valid candidate.
     * Finally, we return one candidate at random and add some noise to its coordinates to make the algorithm less deterministic.
     *
     * @param bb The bounding box of the section
     * @param spawner the SpreadSpawner instance
     * @param config the SpreadSpawnConfig containing the algorithm parameters
     * @return a Location representing the chosen chunk, or null if no suitable chunk could be found
     */
    suspend fun chooseChunkInBB(bb: BoundingBox, spawner: SpreadSpawner, config: SpreadSpawnConfig): Location? {
        data class Candidate(val x: Int, val z: Int, val score: Double)
        val radius = config.spreadRadius
        val sectionX = bb.minX.toInt()..bb.maxX.toInt()
        val sectionZ = bb.minZ.toInt()..bb.maxZ.toInt()
        val splitSize = config.splitSize
        val noiseRange = config.spawnNoise * 16
        val sectionCount = spawner.db.read { spawner.dao.countSpawnsInBB(spawner.world, bb) }
        val candidates = mutableListOf<Candidate>()
        val random = Random.Default
        val allCandidates = mutableListOf<Pair<Int, Int>>()
        val sampleSize = (sectionX.last / splitSize) * (sectionX.last / splitSize)
        val scoreThreshold = radius * radius

        // we calculate this here to save a sectionCount query
        if (sectionCount >= config.spawnCap)
            return null

        // generate candidates
        val xRange = (sectionX.first / splitSize)..(sectionX.last / splitSize)
        val zRange = (sectionZ.first / splitSize)..(sectionZ.last / splitSize)
        val sampledCandidates = generateSequence {
            (xRange.random() * splitSize) to (zRange.random() * splitSize)
        }.distinct().take(sampleSize)

        // candidate analysis
        for ((x, z) in sampledCandidates) {
            if (sectionCount == 0) {
                candidates.add(Candidate(x, z, 0.0))
                continue
            }
            val minDistSq: Double = findNearestSq(x, z, spawner.db, spawner.dao, spawner.world)
            if (minDistSq < scoreThreshold) continue
            candidates.add(Candidate(x, z, minDistSq))
        }

        // candidate selection
        if (candidates.isEmpty())
            return null
        val chosen = candidates.random(random)
        // add random noise to the chosen location to make it less predictable
        val noisyX = (chosen.x + random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionX)
        val noisyZ = (chosen.z + random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionZ)
        return Location(
            spawner.world,
            noisyX.toDouble(),
            0.0,
            noisyZ.toDouble()
        )
    }

    private suspend fun findNearestSq(x: Int, z: Int, database: Database, dao: SpawnLocationsDAO, world: World): Double {
        var minDistSq = Double.MAX_VALUE
        database.read {
            val loc = Location(world, x.toDouble(), 0.0, z.toDouble())
            val spawns = dao.getSpawnsNear(loc, 1000.0)
            for (spawn in spawns) {
                val dx = spawn.location.x - x
                val dz = spawn.location.z - z
                val distSq = dx * dx + dz * dz
                if (distSq < minDistSq) minDistSq = distSq
            }
        }
        return minDistSq
    }

}




