package com.mineinabyss.geary.papermc.creativetabs

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class CreativeTabsListener(private val channel: CreativeTabsChannel) : Listener {
    @EventHandler
    fun NexoItemsLoadedEvent.onItemsLoaded() {
        channel.rebuildOnceLoaded()
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        channel.forget(player)
    }
}
