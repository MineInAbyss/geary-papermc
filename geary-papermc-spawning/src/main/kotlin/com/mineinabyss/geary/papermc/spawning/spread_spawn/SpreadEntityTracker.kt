package com.mineinabyss.geary.papermc.spawning.spread_spawn

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

/** Main thread only */
class SpreadEntityTracker(plugin: Plugin) {
    data class Tag(val id: Long, val createdTime: Long)

    private class Tracked(val entity: Entity, val createdTime: Long)

    private val idKey = NamespacedKey(plugin, "spread_spawn_id")

    // rtree_i32 hands out max(rowid) + 1, so ids are reused once the newest row is deleted
    private val createdKey = NamespacedKey(plugin, "spread_spawn_created")
    private val live = HashMap<Long, Tracked>()
    private val pendingDelete = HashMap<Long, Int>()

    // Rows whose chunk has its entities loaded, by chunk key then row id
    private val loadedRows = HashMap<Long, HashMap<Long, SpreadSpawnLocation>>()

    fun tagOf(entity: Entity): Tag? {
        val data = entity.persistentDataContainer
        val id = data.get(idKey, PersistentDataType.LONG) ?: return null
        return Tag(id, data.get(createdKey, PersistentDataType.LONG) ?: -1)
    }

    /** Removes the tracked entity when it belongs to an older row that had the same id */
    fun isLive(row: SpreadSpawnLocation): Boolean {
        val tracked = live[row.id] ?: return false
        if (!tracked.entity.isValid) {
            live.remove(row.id)
            return false
        }

        if (tracked.createdTime != row.createdTime) {
            tracked.entity.remove()
            live.remove(row.id)
            return false
        }

        return true
    }

    /** False when the entity is not tagged for [row] or another entity already holds it */
    fun register(row: SpreadSpawnLocation, entity: Entity): Boolean {
        if (tagOf(entity) != Tag(row.id, row.createdTime)) return false
        if (isLive(row)) return live[row.id]?.entity == entity

        live[row.id] = Tracked(entity, row.createdTime)
        return true
    }

    fun spawnIfAbsent(row: SpreadSpawnLocation) {
        if (row.id in pendingDelete || isLive(row)) return
        val entity = row.spawn() ?: return
        // The row respawns the entity on chunk load, so saving it with the chunk would only create a duplicate
        entity.isPersistent = false
        entity.persistentDataContainer.set(idKey, PersistentDataType.LONG, row.id)
        entity.persistentDataContainer.set(createdKey, PersistentDataType.LONG, row.createdTime)
        live[row.id] = Tracked(entity, row.createdTime)
    }

    fun unregister(id: Long, entity: Entity) {
        if (live[id]?.entity == entity) live.remove(id)
    }

    /** Keeps [ids] from respawning until [delete] finishes, must resume on the main thread */
    suspend fun <T> deleting(ids: Collection<Long>, delete: suspend () -> T): T {
        ids.forEach { pendingDelete.merge(it, 1, Int::plus) }
        try {
            return delete()
        } finally {
            ids.forEach { id -> pendingDelete.computeIfPresent(id) { _, count -> (count - 1).takeIf { it > 0 } } }
        }
    }

    /** Merges rather than replaces, so a row added while the chunk's rows were being queried is kept */
    fun rowsLoaded(rows: Collection<SpreadSpawnLocation>) {
        for (row in rows) loadedRows.getOrPut(row.pos.chunkKey) { HashMap() }[row.id] = row
    }

    fun chunkUnloaded(chunkKey: Long) {
        loadedRows.remove(chunkKey)
    }

    fun forget(id: Long, createdTime: Long) {
        for (rows in loadedRows.values) {
            if (rows[id]?.createdTime == createdTime) rows.remove(id)
        }
        loadedRows.values.removeIf { it.isEmpty() }
    }

    /** Rows in loaded chunks that have no live entity and are not being deleted */
    fun awaitingRows(): List<SpreadSpawnLocation> =
        loadedRows.values.flatMap { it.values }.filter { it.id !in pendingDelete && !isLive(it) }

    fun liveIds(): Set<Long> {
        live.values.removeIf { !it.entity.isValid }
        return live.keys.toSet()
    }
}
