package com.mineinabyss.geary.papermc.spawning.actions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:ghost_seek")
class GhostSeekAction(
    val type: String = "praying_skeleton",
    val radii: Map<Double, Tasks>,
) : Action {
    @Transient
    private val sortedRadii = radii.entries.sortedBy { it.key }

    override fun ActionGroupContext.execute() {
        val feature = gearyPaper.features.getOrNull<SpawningFeature>() ?: return
        val spawner = feature.spreadSpawnTask?.spreadSpawner ?: return
        val player = entity?.get<Player>() ?: return

        gearyPaper.plugin.launch {
            // Represents first radius which contains any skeletons within it
            val greatestRadius = sortedRadii.firstOrNull {
                spawner.countNearby(player.location, it.key, type) > 0
            } ?: return@launch

            // If we found an entry, execute its actions
            with(greatestRadius.value) { execute() }
        }
    }
}
