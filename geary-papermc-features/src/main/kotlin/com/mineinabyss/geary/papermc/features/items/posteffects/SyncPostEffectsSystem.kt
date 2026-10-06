package com.mineinabyss.geary.papermc.features.items.posteffects

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.features.common.inventory.passiveComponents
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player

private val INTERVAL = 5.ticks

fun WorldScoped.syncPostEffectsSystem() = system(query<Player>())
    .every(INTERVAL)
    .exec { (player) ->
        val effects = player.passiveComponents<PostEffects>()
        PlayerPostEffects.sync(player, if (effects.isEmpty()) emptySet() else effects.flatMapTo(mutableSetOf()) { it.effects })
    }
