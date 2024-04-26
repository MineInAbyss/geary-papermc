package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.messaging.info
import org.bukkit.Bukkit

fun Command.locate() {
    "locate" {
        val mobKey by optionArg(options = gearyMobs.prefabs.map { it.key.toString() }) {
            parseErrorMessage = { "No such entity: $passed" }
        }
        val radius by intArg {
            name = "radius to check"
            default = 0
        }
        playerAction {
            val key = PrefabKey.of(mobKey)
            if (radius <= 0) {
                Bukkit.getWorlds().forEach { world ->
                    world.entities.filter { it.toGeary().deepInstanceOf(key.toEntity()) }.forEach { entity ->
                        val (x, y, z) = entity.location.toBlockLocation().toVector()
                        player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z</aqua> in ${entity.world.name}")
                    }
                }
            } else {
                player.location.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())
                    .filter { it.toGeary().deepInstanceOf(key.toEntity()) }.forEach { entity ->
                        val (x, y, z) = entity.location.toBlockLocation().toVector()
                        player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z")
                    }
            }
        }
    }
}
