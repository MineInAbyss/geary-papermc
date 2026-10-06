package com.mineinabyss.geary.papermc.features.items.posteffects

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import java.util.UUID

/** Only adds and removes single keys, since `postEffects().set` would wipe chains other features applied */
object PlayerPostEffects {
    private val applied = mutableMapOf<UUID, Set<Key>>()

    fun sync(player: Player, wanted: Set<Key>) {
        val current = applied[player.uniqueId].orEmpty()
        if (current == wanted) return

        val postEffects = player.postEffects()
        (current - wanted).forEach(postEffects::remove)
        val added = (wanted - current).filterTo(mutableSetOf(), postEffects::add)

        val now = (current intersect wanted) + added
        if (now.isEmpty()) applied.remove(player.uniqueId)
        else applied[player.uniqueId] = now
    }

    // The server saves post effects with the player, leaving them on at quit would bring them back without the item
    fun clear(player: Player) {
        val current = applied.remove(player.uniqueId) ?: return
        val postEffects = player.postEffects()
        current.forEach(postEffects::remove)
    }
}
