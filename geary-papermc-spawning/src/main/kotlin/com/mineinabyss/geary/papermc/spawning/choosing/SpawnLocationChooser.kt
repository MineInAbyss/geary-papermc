package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.random.Random

class SpawnLocationChooser(
    val config: SpawnConfig.Range,
) {
    private val minSquared = config.minDistance * config.minDistance
    private val horizontalRange = config.minDistance..config.maxDistance
    private val verticalRange = config.minDistance..config.maxVerticalDistance
    fun chooseSpawnLocationNear(onlinePlayers: List<Player>, location: Location): Location? {
        // Pick near current player
        val spawnLocation = location.clone().add(
            randomSign() * horizontalRange.randomOrMin().toDouble(),
            randomSign() * verticalRange.randomOrMin().toDouble(),
            randomSign() * horizontalRange.randomOrMin().toDouble(),
        )

        if (Random.nextDouble() < 0.2) highestBlockWithinYRange(spawnLocation, config.maxVerticalDistance)

        // Ensure not near ANY player
        if (onlinePlayers.any { it.location.distanceSquared(spawnLocation) < minSquared })
            return null

        return spawnLocation
    }

    fun highestBlockWithinYRange(location: Location, range: Int) {
        val highestY = location.world.getHighestBlockYAt(location) + 1.0
        when {
            abs(highestY - location.y) <= range -> location.y = highestY
            location.block.isPassable -> (location.y.toInt() downTo location.y.toInt() - range).forEach {
                location.y = it.toDouble()
                if (!location.block.isPassable) location.y += 1
            }
        }
    }

    private fun randomSign() = if (Random.nextBoolean()) 1 else -1
}
