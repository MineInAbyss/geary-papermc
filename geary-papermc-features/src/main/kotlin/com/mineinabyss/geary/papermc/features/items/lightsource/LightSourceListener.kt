package com.mineinabyss.geary.papermc.features.items.lightsource

import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent

class LightSourceListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerMoveEvent.onMove() {
        if (!PlayerLightBlocks.hasLight(player)) return
        val moved = hasExplicitlyChangedBlock() || (hasChangedOrientation() && PlayerLightBlocks.tracksOrientation(player))
        if (moved) PlayerLightBlocks.refresh(player, to, recomputeViewers = false)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerTeleportEvent.onTeleport() {
        if (to.world == from.world) PlayerLightBlocks.refresh(player, to, recomputeViewers = false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerRespawnEvent.onRespawn() = PlayerLightBlocks.clear(player)

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerChangedWorldEvent.onChangeWorld() = PlayerLightBlocks.clear(player)

    @EventHandler
    fun PlayerChunkLoadEvent.onChunkSent() = PlayerLightBlocks.resend(player, chunk)

    @EventHandler
    fun PlayerQuitEvent.onQuit() = PlayerLightBlocks.release(player)
}
