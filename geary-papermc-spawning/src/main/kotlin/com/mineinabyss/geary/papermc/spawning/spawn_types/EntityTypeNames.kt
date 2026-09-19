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

    // Misses are not cached since MythicMobs attaches its data after entities load
    fun of(entity: Entity): String? {
        cache.getIfPresent(entity.uniqueId)?.let { return it }
        val type = GearyReadEntityTypeEvent(entity).apply { call() }.type ?: return null
        cache.put(entity.uniqueId, type)
        return type
    }
}
