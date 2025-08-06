package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.spawning.database.dao.SpawnLocationsDAO
import me.dvyy.sqlite.Database
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

/*
 * Goal of this file is to provide a way to calculate the chunks we want to spawn entities in
 */


/**
 * given a list of chunks, return one single chunk that is valid for spawning
 * ie: that has no entities within a certain radius
 */

suspend fun getValidChunk(targetedSpawner: SpreadSpawner, db: Database): Location? {
    val radius = targetedSpawner.minRadius
    val sectionX = targetedSpawner.sectionXRange
    val sectionZ = targetedSpawner.sectionZRange
    val splitSize = targetedSpawner.splitSize
    val noiseFactor = targetedSpawner.noiseFactor
    val noiseRange = targetedSpawner.noiseRange
    val corner1 = Vector(sectionX.first.toDouble(), 0.0, sectionZ.first.toDouble())
    val corner2 = Vector(sectionX.last.toDouble(), 256.0, sectionZ.last.toDouble())
    val sectionBB = BoundingBox.of(corner1, corner2)
    val sectionCount = db.read {
        targetedSpawner.dao.countSpawnsInBB(targetedSpawner.world, sectionBB)
    }

    data class Candidate(val x: Int, val z: Int, val score: Double)
    val candidates = mutableListOf<Candidate>()
    val random = Random.Default
    val allCandidates = mutableListOf<Pair<Int, Int>>()
    for (x in sectionX step splitSize) {
        for (z in sectionZ step splitSize) {
            allCandidates.add(x to z)
        }
    }
    val sampleSize = (sectionX.last / splitSize) * (sectionX.last / splitSize)
    val sampledCandidates = allCandidates.shuffled().take(sampleSize)
    val scoreThreshold = radius * radius

    for ((x, z) in sampledCandidates) {
        if (sectionCount == 0) {
            candidates.add(Candidate(x, z, 0.0))
            println("lol this is really funny if thats the bug")
            continue
        }
        val minDistSq: Double = findNearestSq(x, z, db, targetedSpawner.dao, targetedSpawner.world)
        if (minDistSq < scoreThreshold) continue
        val noise = random.nextDouble(0.0, noiseFactor)
        val score = minDistSq + noise
        if (score >= scoreThreshold) {
            candidates.add(Candidate(x, z, score))
        }
    }
    if (candidates.isEmpty()) return null
    val chosen = candidates.random(random)
    val noisyX = (chosen.x + random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionX)
    val noisyZ = (chosen.z + random.nextInt(-noiseRange, noiseRange + 1)).coerceIn(sectionZ)
    return Location(
        targetedSpawner.world,
        noisyX.toDouble(),
        0.0,
        noisyZ.toDouble()
    )
}


suspend fun findNearestSq(x: Int, z: Int, database: Database, dao: SpawnLocationsDAO, world: World): Double {
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