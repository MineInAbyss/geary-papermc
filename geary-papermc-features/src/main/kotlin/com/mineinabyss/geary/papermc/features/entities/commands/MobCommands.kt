package com.mineinabyss.geary.papermc.features.entities.commands

import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.features.DICommandContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

context(context: DICommandContext)
internal fun IdoCommand.mobs() = ("mobs" / "m") {
    permission = "geary.mobs"
    ("spawn" / "s") {
        permission = "geary.mobs.spawn"
        executes.asPlayer().args("mob" to GearyArgs.mob(), "number" to Args.integer(min = 0)) { mob, numOfSpawns ->
            val key = mob.get<PrefabKey>()
            var spawned = 0
            repeat(numOfSpawns) {
                player.location.spawnFromPrefab(mob)
                    .onSuccess { spawned++ }
                    .onFailure { it.printStackTrace() }
            }
            when (spawned) {
                numOfSpawns -> sender.success("Spawned $spawned $key")
                0 -> sender.error("Failed to spawn $key, see console for details")
                else -> sender.error("Only spawned $spawned/$numOfSpawns $key, see console for details")
            }
        }
    }
    locate()
    mobsQuery()
}
