package com.mineinabyss.geary.papermc.features.common.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.math.pow

@Serializable
@SerialName("geary:ghost_seek")
class GhostSeekAction(val radii: List<Int>) : Action {

    override fun ActionGroupContext.execute() {
        val feature = gearyPaper.features.getOrNull<SpawningFeature>() ?: return
        val spawner = feature.spreadSpawnTask?.spreadSpawner ?: return
        val player = entity?.get<Player>() ?: return

        gearyPaper.plugin.launch {
            val pings = mutableListOf<Int>()
            for (i in radii.indices.reversed()) {
                val radius = radii[i]
                val nearby = spawner.getNBNear(player.location, radius.toDouble())
                if (nearby > 0) {
                    pings.add(i)
                }
            }

            for (pingIndex in pings) {
                player.playSound(
                    player.location,
                    Sound.BLOCK_NOTE_BLOCK_BELL,
                    1.0f,
                    2.0.pow((pingIndex - 2) / 12.0).toFloat()
                )
                delay(300)
            }
        }
    }
}