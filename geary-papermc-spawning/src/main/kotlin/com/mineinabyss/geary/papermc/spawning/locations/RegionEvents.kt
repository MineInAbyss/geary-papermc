package com.mineinabyss.geary.papermc.spawning.locations

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Fired by [RegionTracker] when a player enters a region.
 */
class PlayerEnterRegionEvent(
    player: Player,
    val regionId: String,
) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * Fired by [RegionTracker] when a player exits a region,
 * including on player quit while inside one.
 */
class PlayerExitRegionEvent(
    player: Player,
    val regionId: String,
) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
