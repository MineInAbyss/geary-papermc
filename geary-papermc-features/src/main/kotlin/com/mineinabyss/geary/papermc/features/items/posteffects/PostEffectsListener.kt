package com.mineinabyss.geary.papermc.features.items.posteffects

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PostEffectsListener : Listener {
    @EventHandler
    fun PlayerQuitEvent.onQuit() = PlayerPostEffects.clear(player)
}
