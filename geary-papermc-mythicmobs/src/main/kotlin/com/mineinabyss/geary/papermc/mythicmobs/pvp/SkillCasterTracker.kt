package com.mineinabyss.geary.papermc.mythicmobs.pvp

import org.bukkit.entity.Player

/**
 * Tracks the player whose skill is executing on the main thread, so effects MythicMobs applies without
 * attribution can still be traced back to a caster. Mechanics that defer to later ticks are not covered
 */
object SkillCasterTracker {
    var current: Player? = null
        private set

    fun <T> withCaster(player: Player?, block: () -> T): T {
        val previous = current
        current = player
        try {
            return block()
        } finally {
            current = previous
        }
    }
}
