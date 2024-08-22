package com.mineinabyss.geary.papermc.spawning

import com.mineinabyss.geary.papermc.spawning.choosing.SpawnChooser
import com.mineinabyss.geary.papermc.spawning.config.SpawnPosition
import com.mineinabyss.idofront.util.randomOrMin
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.MobExecutor
import org.bukkit.Location

class MobSpawner(
    val spawnChooser: SpawnChooser,
) {
    fun attemptSpawnAt(location: Location, position: SpawnPosition) {
        val spawn = spawnChooser.chooseAllowedSpawnNear(location, position) ?: return
//        if(!spawn.conditions.areMet(ActionGroupContext(TODO("Allow making context from just location")))) return
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
                    true,
                )
            )
            spawn.type.spawnAt(spawnLoc)
        }
    }
}