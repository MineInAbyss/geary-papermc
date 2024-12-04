package com.mineinabyss.geary.papermc.plugin.commands.mobs

import com.mineinabyss.geary.papermc.plugin.commands.GearyArgs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.messaging.error

internal fun IdoCommand.mobs() = ("mobs" / "m") {
    requiresPermission("geary.mobs")
    ("spawn" / "s") {
        requiresPermission("geary.mobs.spawn")
        playerExecutes(GearyArgs.mob(), Args.integer(min = 0)) { mob, numOfSpawns ->
            val key = mob.get<PrefabKey>()
            repeat(numOfSpawns) {
                player.location.spawnFromPrefab(mob).onFailure {
                    sender.error("Failed to spawn $key")
                    it.printStackTrace()
                }
            }
        }
    }
    locate()
    mobsQuery()
}
