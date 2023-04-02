package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import kotlin.collections.set

class BukkitEntity2Geary {
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
        require(bukkit.toNMS().valid) { "Tried to access Geary entity for an entity that was not valid: $bukkit" }
        return get(bukkit) ?: entity { set(bukkit) }
    }
}
