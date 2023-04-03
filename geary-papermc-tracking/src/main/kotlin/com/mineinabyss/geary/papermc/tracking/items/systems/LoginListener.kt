package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class LoginListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.track() {
        val entity = player.toGeary()
        entity.set(PlayerItemCache(entity))
    }
}
