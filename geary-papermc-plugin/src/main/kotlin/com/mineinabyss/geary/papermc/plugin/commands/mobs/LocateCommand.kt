package com.mineinabyss.geary.papermc.plugin.commands.mobs

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.commands.filterPrefabs
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.helpers.getKeys
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOf
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.messaging.info
import org.bukkit.Bukkit

fun IdoCommand.locate() = "locate" {
    requiresPermission("geary.mobs.locate")
    val mobKeyArg by ArgsMinecraft.namespacedKey().suggests {
        val gearyMobs = gearyPaper.worldManager.global.getAddon(EntityTracking)
        suggest(gearyMobs.query.prefabs.getKeys().filterPrefabs(suggestions.remaining))
    }
    val radiusArg by Args.integer(min = 0)
    playerExecutes {
        val geary = player.world.toGeary()
        val radius = radiusArg()
        val key = mobKeyArg()?.let { PrefabKey.of(it.asString()) } ?: commandException("No such mob key")
        if (radius <= 0) {
            Bukkit.getWorlds().forEach { world ->
                world.entities.filter { it.toGeary().deepInstanceOf(geary.entityOf(key)) }.forEach { entity ->
                    val (x, y, z) = entity.location.toBlockLocation().toVector()
                    player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z</aqua> in ${entity.world.name}")
                }
            }
        } else {
            player.location.getNearbyEntities(radius.toDouble(), radius.toDouble(), radius.toDouble())
                .filter { it.toGeary().deepInstanceOf(geary.entityOf(key)) }.forEach { entity ->
                    val (x, y, z) = entity.location.toBlockLocation().toVector()
                    player.info("<gold>Found <yellow>${key.key}</yellow> at <click:run_command:/teleport $x $y $z><aqua>$x,$y,$z")
                }
        }
    }
}
