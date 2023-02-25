package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.GearyItemCache
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class LoginListener : Listener {
    @EventHandler
    fun PlayerLoginEvent.track() {
        val entity = player.toGeary()
        entity.set(GearyItemCache(entity))
        player.inventory.itemInMainHand
    }
}
