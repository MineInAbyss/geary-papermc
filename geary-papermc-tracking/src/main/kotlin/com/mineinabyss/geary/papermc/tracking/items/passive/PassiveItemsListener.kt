package com.mineinabyss.geary.papermc.tracking.items.passive

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PassiveItemsListener : Listener {
    // Runs before the geary player tracker encodes components at HIGHEST
    @EventHandler(priority = EventPriority.LOW)
    fun PlayerQuitEvent.savePassiveItems() {
        PassiveSlots.save(player)
    }
}
