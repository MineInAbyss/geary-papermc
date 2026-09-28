package com.mineinabyss.geary.papermc.tracking.items.passive

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Called before a player's unlocked passive slot count changes
 */
class PassiveSlotsChangeEvent(
    player: Player,
    val oldCount: Int,
    val newCount: Int,
) : PlayerEvent(player), Cancellable {
    private var cancelled = false
    override fun isCancelled() = cancelled
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
