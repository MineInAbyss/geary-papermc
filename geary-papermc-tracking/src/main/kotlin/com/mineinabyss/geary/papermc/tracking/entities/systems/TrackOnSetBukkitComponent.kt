package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.typealiases.BukkitEntity

class TrackOnSetBukkitComponent : GearyListener() {
    private val TargetScope.bukkit by onSet<BukkitEntity>()

    @Handler
    fun TargetScope.loadEntity() {
        gearyMobs.bukkit2Geary[bukkit] = entity

        // Load persisted components
        val pdc = bukkit.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            entity.loadComponentsFrom(pdc)

        // allow us to both get the BukkitEntity and specific class (ex Player)
        bukkit.type.entityClass?.kotlin?.let { bukkitClass ->
            entity.set(bukkit, bukkitClass)
        }

        entity.set(bukkit.uniqueId)
    }
}
