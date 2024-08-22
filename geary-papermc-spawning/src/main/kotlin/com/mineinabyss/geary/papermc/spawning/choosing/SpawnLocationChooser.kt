package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.random.Random

class SpawnLocationChooser(
    val config: SpawnConfig.Range,
) {
    fun chooseSpawnLocationNear(onlinePlayers: List<Player>, location: Location): Location? {
        val horizontalRange = config.minDistance..config.maxDistance
        val verticalRange = config.minDistance..config.maxVerticalDistance

        // Pick near current player
        val spawnLocation = location.add(
            randomSign() * horizontalRange.randomOrMin().toDouble(),
            randomSign() * verticalRange.randomOrMin().toDouble(),
            randomSign() * horizontalRange.randomOrMin().toDouble(),
        )

//        spawnLocation.y = spawnLocation.y.coerceAtMost(location.world.getHighestBlockAt(spawnLocation).y.toDouble())

        // Ensure not near ANY player
        if (onlinePlayers.any { it.location.distanceSquared(spawnLocation) < config.minDistance * config.minDistance })
            return null

        return spawnLocation
    }

    private fun randomSign() = if (Random.nextBoolean()) 1 else -1
}
