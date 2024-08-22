package com.mineinabyss.geary.papermc.spawning

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.choosing.LocationSpread
import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.util.randomOrMin
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.MobExecutor
import org.bukkit.Location

class MobSpawner(
    val spawnChooser: SpawnChooser,
    val spread: LocationSpread,
) {
    fun attemptSpawnAt(location: Location, position: SpawnPosition): Boolean {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position) ?: return false

        // Check dynamic conditions
        if (!spawn.conditions.conditionsMet(
                ActionGroupContext().apply {
                    this.location = location.clone()
                    environment["spawnTypes"] = listOf(spawn.type.key)
                })
        ) return false

        repeat(spawn.amount.randomOrMin()) {
            val radius = spawn.radius.randomOrMin().toDouble()
            val spawnLoc = if (radius == 1.0) location
            else BukkitAdapter.adapt(
                MobExecutor.findSafeSpawnLocation(
                    BukkitAdapter.adapt(location),
                    radius,
                    radius,
                    2,
                    false,
                    position == SpawnPosition.GROUND,
                )
            )
            val spawned = spawn.type.spawnAt(spawnLoc)

            val nonSuffocatingLoc = spread.ensureSuitableLocationOrNull(
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
