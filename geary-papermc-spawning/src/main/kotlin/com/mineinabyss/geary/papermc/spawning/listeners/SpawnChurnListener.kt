package com.mineinabyss.geary.papermc.spawning.listeners

import io.lumine.mythic.bukkit.MythicBukkit
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent

/** Counts mobs the spawn task spawns against mobs the server despawns, to measure despawn churn. Main thread only */
class SpawnChurnListener(val enabled: Boolean) : Listener {
    // Ids of live mobs the spawn task spawned since the last reset
    private val spawnedIds = IntOpenHashSet()
    private val mobRegistry by lazy { MythicBukkit.inst().mobManager.mobRegistry }

    var spawned = 0L
        private set
    var despawnedSpawned = 0L
        private set
    var despawnedMythic = 0L
        private set
    var sinceTick = Bukkit.getCurrentTick()
        private set

    fun recordSpawn(entity: Entity) {
        if (!enabled) return
        spawned++
        spawnedIds.add(entity.entityId)
    }

    fun reset() {
        spawnedIds.clear()
        spawned = 0
        despawnedSpawned = 0
        despawnedMythic = 0
        sinceTick = Bukkit.getCurrentTick()
    }

    // Lowest so MythicMobs has not dropped the mob from its registry yet
    @EventHandler(priority = EventPriority.LOWEST)
    fun EntityRemoveEvent.onRemove() {
        val wasSpawned = spawnedIds.remove(entity.entityId)
        if (cause != EntityRemoveEvent.Cause.DESPAWN) return
        if (wasSpawned) despawnedSpawned++
        if (mobRegistry.isActiveMob(entity.uniqueId)) despawnedMythic++
    }
}
