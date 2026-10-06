package com.mineinabyss.geary.papermc.features.items.lightsource

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.features.common.inventory.passiveComponents
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player

private val INTERVAL = 5.ticks

/** Picks up items entering or leaving the passive slots, which fire no move event */
fun WorldScoped.syncLightSourceSystem() = system(query<Player>())
    .every(INTERVAL)
    .exec { (player) -> PlayerLightBlocks.update(player, player.passiveComponents<LightSource>().maxByOrNull { it.level }) }
