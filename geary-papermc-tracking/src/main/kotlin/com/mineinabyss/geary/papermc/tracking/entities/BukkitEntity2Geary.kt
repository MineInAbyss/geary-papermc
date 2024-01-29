package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.components.AddedToWorld
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.idofront.typealiases.BukkitEntity
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import org.spigotmc.AsyncCatcher
import kotlin.collections.set

class BukkitEntity2Geary(val forceMainThread: Boolean = true) {
    private val entityMap = Int2LongOpenHashMap().apply { defaultReturnValue(-1) }

    operator fun get(bukkit: BukkitEntity): GearyEntity? {
        val id = entityMap.get(bukkit.entityId)
        if (id == -1L) return null
        return id.toGeary()
    }

    operator fun set(bukkit: BukkitEntity, entity: GearyEntity) {
        entityMap[bukkit.entityId] = entity.id.toLong()
    }

    operator fun contains(entityId: Int): Boolean = entityMap.containsKey(entityId)

    fun remove(entityId: Int): GearyEntity? {
        return entityMap.remove(entityId).takeIf { it != -1L }?.toGeary()
    }

    fun getOrCreate(bukkit: BukkitEntity): GearyEntity {
        return get(bukkit) ?: run {
            if (forceMainThread) AsyncCatcher.catchOp("Async geary entity creation for id ${bukkit.entityId}, type ${bukkit.type}")
            synchronized(entityMap) {
                entity { set(bukkit) }.also { fireAddToWorldEvent(bukkit, it) }
            }
        }
    }

    fun fireAddToWorldEvent(bukkit: BukkitEntity, entity: GearyEntity) {
        entity.add<AddedToWorld>()
        GearyEntityAddToWorldEvent(entity, bukkit).callEvent()
        entity.encodeComponentsTo(bukkit)
    }
}
