package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.random.Random

class SpawnLocationChooser(
    val config: SpawnConfig.Range,
) {
    fun chooseSpawnLocationNear(onlinePlayers: List<Player>, location: Location): Location? {
        val horizontalRange = config.minDistance..config.maxDistance
        val verticalRange = config.minDistance..config.maxVerticalDistance

        // Pick near current player
        val spawnLocation = location.clone().add(
            randomSign() * horizontalRange.randomOrMin().toDouble(),
            randomSign() * verticalRange.randomOrMin().toDouble(),
            randomSign() * horizontalRange.randomOrMin().toDouble(),
        )

        if (Random.nextDouble() < 0.2) {
            spawnLocation.y = tryGetHighestBlockWithinYRange(spawnLocation, config.maxVerticalDistance).y
        }

        // Ensure not near ANY player
        if (onlinePlayers.any { it.location.distanceSquared(spawnLocation) < config.minDistance * config.minDistance })
            return null

        return spawnLocation
    }

    fun tryGetHighestBlockWithinYRange(location: Location, range: Int): Location {
        val newLoc = location.clone()
        val highestY = newLoc.world.getHighestBlockAt(newLoc).y.toDouble() + 1
        if (abs(highestY - newLoc.y) <= range) return newLoc.apply { y = highestY }
        if (!newLoc.block.isPassable) return newLoc
        (newLoc.y.toInt() downTo newLoc.y.toInt() - range)
            .forEach {
                newLoc.y = it.toDouble()
                if (!newLoc.block.isPassable) return newLoc.up(1)
            }
        return location
    }

    private fun randomSign() = if (Random.nextBoolean()) 1 else -1
}
