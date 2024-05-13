package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataContainer


fun Location.spawnFromPrefab(prefab: PrefabKey, initEntityPreEvent: GearyEntity.() -> Unit = {}): Result<BukkitEntity> {
    val entity = prefabs.manager[prefab] ?: return Result.failure(IllegalArgumentException("No prefab found"))
    return spawnFromPrefab(entity, initEntityPreEvent = initEntityPreEvent)
}


fun Location.spawnFromPrefab(
    prefab: GearyEntity,
    existingPDC: PersistentDataContainer? = null,
    initEntityPreEvent: GearyEntity.() -> Unit = {}
): Result<BukkitEntity> {
    return runCatching {
        val entity = entity {
            if (existingPDC != null) loadComponentsFrom(existingPDC)
            extend(prefab)
            initEntityPreEvent()
            emit(AttemptSpawn(this@spawnFromPrefab))
        }
        val bukkit = entity.get<BukkitEntity>() ?: error("Entity was not created when spawning from prefab")
        gearyMobs.bukkit2Geary.fireAddToWorldEvent(bukkit, entity)
        bukkit
    }
}
