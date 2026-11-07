package com.mineinabyss.geary.papermc.spawning.spawn_types

import com.google.common.cache.CacheBuilder
import com.mineinabyss.idofront.events.call
import org.bukkit.entity.Entity
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/** Resolves an entity's spawn type name through [GearyReadEntityTypeEvent], cached since firing an event per entity per scan is costly. */
object EntityTypeNames {
    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(30.seconds.toJavaDuration())
        .build<UUID, String>()

    // Misses expire quickly since MythicMobs attaches its data after entities load
    private val misses = CacheBuilder.newBuilder()
        .expireAfterWrite(5.seconds.toJavaDuration())
        .build<UUID, Unit>()

    fun of(entity: Entity): String? {
        val uuid = entity.uniqueId
        cache.getIfPresent(uuid)?.let { return it }
        if (misses.getIfPresent(uuid) != null) return null
        val type = resolve(entity)
        if (type == null) misses.put(uuid, Unit) else cache.put(uuid, type)
        return type
    }

    /** Uncached, for callers that keep their own cache */
    fun resolve(entity: Entity): String? = GearyReadEntityTypeEvent(entity).apply { call() }.type
}
