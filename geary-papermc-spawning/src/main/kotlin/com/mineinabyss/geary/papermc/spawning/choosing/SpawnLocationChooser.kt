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
            val highestY = location.world.getHighestBlockAt(spawnLocation).y.toDouble() + 1
            if (abs(highestY - location.y) <= verticalRange.last)
                spawnLocation.y = highestY
        }

        // Ensure not near ANY player
        if (onlinePlayers.any { it.location.distanceSquared(spawnLocation) < config.minDistance * config.minDistance })
            return null

        return spawnLocation
    }

    private fun randomSign() = if (Random.nextBoolean()) 1 else -1
}
