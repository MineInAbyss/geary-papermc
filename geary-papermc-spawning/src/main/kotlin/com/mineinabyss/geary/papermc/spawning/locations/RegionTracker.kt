package com.mineinabyss.geary.papermc.spawning.locations

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RegionTracker(
    private val regions: RegionService,
) : Listener {
    private val lastRegions = ConcurrentHashMap<UUID, Set<String>>()

    private fun update(player: Player, at: Location) {
        val now = regions.regionsAt(at)
        val prev = lastRegions.put(player.uniqueId, now) ?: emptySet()
        if (now == prev) return
        (prev - now).forEach { PlayerExitRegionEvent(player, it).callEvent() }
        (now - prev).forEach { PlayerEnterRegionEvent(player, it).callEvent() }
    }

    @EventHandler
    fun PlayerMoveEvent.onMove() {
        if (!hasExplicitlyChangedBlock()) return
        update(player, to)
    }

    @EventHandler
    fun PlayerTeleportEvent.onTeleport() = update(player, to)

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() = update(player, respawnLocation)

    @EventHandler
    fun PlayerJoinEvent.onJoin() = update(player, player.location)

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        val prev = lastRegions.remove(player.uniqueId) ?: return
        prev.forEach { PlayerExitRegionEvent(player, it).callEvent() }
    }
}
