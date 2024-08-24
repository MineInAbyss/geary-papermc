package com.mineinabyss.geary.papermc.plugin.commands.mobs

import com.mineinabyss.geary.papermc.plugin.commands.filterPrefabs
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.messaging.error

internal fun IdoCommand.mobs() = ("mobs" / "m") {
    requiresPermission("geary.mobs")
    ("spawn" / "s") {
        requiresPermission("geary.mobs.spawn")
        val mobKeyArg by ArgsMinecraft.namespacedKey().suggests {
            suggest(gearyMobs.query.spawnablePrefabs.getKeys().filterPrefabs(suggestions.remaining))
        }
        val radiusArg by Args.integer(min = 0)
        val numOfSpawns by Args.integer(min = 0)

        playerExecutes {
            val mobKey = mobKeyArg() ?: return@playerExecutes sender.error("No such mob key")
            val key = PrefabKey.of(mobKey.asString())

            repeat(numOfSpawns()) {
                player.location.spawnFromPrefab(key).onFailure {
                    sender.error("Failed to spawn $key")
                    it.printStackTrace()
                }
            }
        }
    }
    locate()
    mobsQuery()
}
