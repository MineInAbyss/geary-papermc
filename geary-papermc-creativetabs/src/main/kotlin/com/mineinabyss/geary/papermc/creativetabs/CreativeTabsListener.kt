package com.mineinabyss.geary.papermc.creativetabs

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerQuitEvent

class CreativeTabsListener(private val channel: CreativeTabsChannel) : Listener {
    @EventHandler
    fun NexoItemsLoadedEvent.onItemsLoaded() {
        channel.rebuildOnceLoaded()
    }

    // Entering creative is when the tabs are needed, so permission is checked again there
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerGameModeChangeEvent.onGameModeChange() {
        if (newGameMode == GameMode.CREATIVE) channel.send(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        channel.forget(player)
    }
}
