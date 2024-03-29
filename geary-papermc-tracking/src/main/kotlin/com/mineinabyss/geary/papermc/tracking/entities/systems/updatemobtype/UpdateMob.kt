package com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Bukkit

object UpdateMob {
    fun recreateGearyEntity(entity: BukkitEntity) {
        val gearyEntity = entity.toGearyOrNull() ?: return
        gearyEntity.encodeComponentsTo(entity)
        gearyEntity.removeEntity()
        gearyMobs.bukkit2Geary.getOrCreate(entity)
    }

    fun scheduleRecreation(entity: BukkitEntity, gearyEntity: GearyEntity) {
        val loc = entity.location
        val prefab = gearyEntity.prefabs.first()

        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            entity.remove()
        }, 1)
        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            loc.spawnFromPrefab(prefab, entity.persistentDataContainer)
                .getOrThrow()
        }, 10)
    }

    fun scheduleRemove(entity: BukkitEntity) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(gearyPaper.plugin, {
            entity.remove()
        }, 10)
    }
}
