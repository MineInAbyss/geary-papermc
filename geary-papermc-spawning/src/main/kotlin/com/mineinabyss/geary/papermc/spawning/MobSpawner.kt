package com.mineinabyss.geary.papermc.spawning

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.Location

class MobSpawner(
    val spawnChooser: SpawnChooser,
    val spreadRepo: LocationSpread,
) {
    fun attemptSpawnAt(location: Location, position: SpawnPosition): Boolean {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position) ?: return false

        // Check dynamic conditions
        if (!spawn.conditions.all {
                it.conditionsMet(
                    ActionGroupContext().apply {
                        this.location = location.clone()
                        environment["spawnTypes"] = listOf(spawn.type.key)
                    }
                )
            }
        ) return false

        repeat(spawn.amount.randomOrMin()) {
            val spread = spawn.spread.randomOrMin().toDouble()
            val ySpread = spawn.ySpread.randomOrMin().toDouble()
            val spawnLoc = if (spread == 0.0 && ySpread == 0.0) location
            else spreadRepo.getNearbySpawnLocation(position, location, spread, ySpread)

            val spawned = spawn.type.spawnAt(spawnLoc)

            val nonSuffocatingLoc = spreadRepo.ensureSuitableLocationOrNull(
                spawnLoc,
                spawned.boundingBox,
                extraAttemptsUp = 10
            ) ?: run {
                spawned.remove()
                return@repeat
            }
            spawned.teleportAsync(nonSuffocatingLoc)
        }
        return true
    }
}
