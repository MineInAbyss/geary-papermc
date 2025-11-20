package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.tracking.entities.components.AddedToWorld
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityRemoveFromWorldEvent
import com.mineinabyss.idofront.typealiases.BukkitEntity
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import org.spigotmc.AsyncCatcher

class BukkitEntity2Geary(
    val forceMainThread: Boolean = true
) {
    private val entityMap = Int2LongOpenHashMap().apply { defaultReturnValue(-1) }

    context(world: Geary)
    operator fun get(bukkitEntity: BukkitEntity): GearyEntity? = synchronized(entityMap) {
        val id = entityMap.get(bukkitEntity.entityId)
        if (id == -1L) return null
        return with(world) { id.toGeary() }
    }

    context(world: Geary)
    operator fun get(entityId: Int): GearyEntity? = synchronized(entityMap) {
        val id = entityMap.get(entityId)
        if (id == -1L) return null
        return with(world) { id.toGeary() }
    }

    operator fun set(bukkit: BukkitEntity, entity: GearyEntity) = synchronized(entityMap) {
        entityMap[bukkit.entityId] = entity.id.toLong()
    }

    operator fun contains(entityId: Int): Boolean = synchronized(entityMap) { entityMap.containsKey(entityId) }

    fun remove(entityId: Int) = synchronized(entityMap) {
        entityMap.remove(entityId)
    }

    context(world: Geary)
    fun getOrCreate(bukkit: BukkitEntity): GearyEntity = synchronized(entityMap) {
        return get(bukkit) ?: run {
            if (forceMainThread) AsyncCatcher.catchOp("Async geary entity creation for id ${bukkit.entityId}, type ${bukkit.type}")
            synchronized(entityMap) {
                world.entity { set(bukkit) }.also { fireAddToWorldEvent(bukkit, it) }
            }
        }
    }

    context(world: Geary)
    fun fireAddToWorldEvent(bukkit: BukkitEntity, entity: GearyEntity) = synchronized(entityMap) {
        entity.add<AddedToWorld>()
        val entityBinds = world.getAddon(EntityTracking).entityTypeBinds[bukkit.type.key.toString()]
        entityBinds.forEach { bind ->
            entity.extend(bind)
        }
        GearyEntityAddToWorldEvent(entity, bukkit).callEvent()
        entity.encodeComponentsTo(bukkit)
    }

    fun fireRemoveFromWorldEvent(bukkit: BukkitEntity, entity: GearyEntity) = synchronized(entityMap) {
        with(entity.world) {
            entity.remove<AddedToWorld>()
            GearyEntityRemoveFromWorldEvent(entity, bukkit).callEvent()
            entity.encodeComponentsTo(bukkit)
        }
    }
}
