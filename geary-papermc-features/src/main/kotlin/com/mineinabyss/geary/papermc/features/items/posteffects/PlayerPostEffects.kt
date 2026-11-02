package com.mineinabyss.geary.papermc.features.items.posteffects

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

/**
 * Every post effect in the geary namespace is ours, so the player's effects are recalculated from what they wear rather than tracked.
 * Effects in other namespaces belong to other features and are left alone
 */
object PlayerPostEffects {
    fun sync(player: Player, wanted: Set<Key>) {
        val postEffects = player.postEffects()
        postEffects.values()
            .filter { it.namespace() == PostEffects.NAMESPACE && it !in wanted }
            .forEach(postEffects::remove)
        wanted.forEach(postEffects::add)
    }

    fun clear(player: Player) = sync(player, emptySet())
}
