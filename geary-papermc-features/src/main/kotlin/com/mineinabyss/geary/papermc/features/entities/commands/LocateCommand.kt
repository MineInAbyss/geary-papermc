package com.mineinabyss.geary.papermc.features.entities.commands

import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.messaging.info
import org.bukkit.Bukkit

fun IdoCommand.locate() = "locate" {
    permission = "geary.mobs.locate"
    executes.asPlayer().args("mob" to GearyArgs.mob(), "radius" to Args.integer(min = 0).default { 0 }) { mob, radius ->
        val prefabKey = mob.get<PrefabKey>()
        val found = if (radius <= 0) {
            Bukkit.getWorlds().flatMap { world -> world.entities.filter { it.toGeary().deepInstanceOf(mob) } }
        } else {
            player.location.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())
                .filter { it.toGeary().deepInstanceOf(mob) }
        }

        if (found.isEmpty()) {
            player.info(
                "<gold>Found no <yellow>$prefabKey</yellow>" +
                        if (radius <= 0) " in loaded chunks" else " within $radius blocks"
            )
            return@args
        }

        found.forEach { entity ->
            val (x, y, z) = entity.location.toBlockLocation().toVector()
            player.info("<gold>Found <yellow>${prefabKey}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z</aqua> in ${entity.world.name}")
        }
        player.info("<gold>Found <yellow>${found.size}</yellow> total")
    }
}
