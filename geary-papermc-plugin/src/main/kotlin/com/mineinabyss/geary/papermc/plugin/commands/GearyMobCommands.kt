package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeyStrings
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error

fun Command.mobs() = ("mobs" / "m") {
    ("spawn" / "s") {
        val mobKey by optionArg(options = gearyMobs.query.spawnablePrefabs.getKeyStrings()) {
            parseErrorMessage = { "No such entity: $passed" }
        }
        val numOfSpawns by intArg {
            name = "number of spawns"
            default = 1
        }

        playerAction {
            val cappedSpawns = numOfSpawns
            val key = PrefabKey.of(mobKey)

            repeat(cappedSpawns) {
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
