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

        for (x in sectionX step splitSize) {
            for (z in sectionZ step splitSize) {
                allCandidates.add(x to z)
            }
        }

        val sampledCandidates = allCandidates.shuffled().take(sampleSize)

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




