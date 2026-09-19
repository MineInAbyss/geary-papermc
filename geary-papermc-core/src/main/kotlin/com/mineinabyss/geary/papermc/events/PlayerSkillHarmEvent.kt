package com.mineinabyss.geary.papermc.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired before a skill cast by [attacker] harms [victim] through a path that never reaches EntityDamageByEntityEvent,
 * so PvP rules can be applied to it by cancelling
 */
class PlayerSkillHarmEvent(
    val attacker: Player,
    val victim: Player,
    val kind: Kind,
) : Event(), Cancellable {
    enum class Kind { DAMAGE, POTION_EFFECT }

    private var cancelled = false
    override fun isCancelled() = cancelled
    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST
}
