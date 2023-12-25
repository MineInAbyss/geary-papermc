package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataContainer


fun Location.spawnFromPrefab(prefab: PrefabKey): Result<BukkitEntity> {
    val entity = prefabs.manager[prefab] ?: return Result.failure(IllegalArgumentException("No prefab found"))
    return spawnFromPrefab(entity)
}

fun Location.spawnFromPrefab(prefab: GearyEntity, existingPDC: PersistentDataContainer? = null): Result<BukkitEntity> {
    return runCatching {
        entity {
            if (existingPDC != null) loadComponentsFrom(existingPDC)
            addPrefab(prefab)
            callEvent(AttemptSpawn(this@spawnFromPrefab))
        }.get<BukkitEntity>() ?: error("Entity was not created when spawning from prefab")
    }
}
