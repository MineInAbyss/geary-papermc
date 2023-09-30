package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity

class TrackOnSetBukkitComponent : GearyListener() {
    private val Pointers.bukkit by get<BukkitEntity>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        gearyMobs.bukkit2Geary[bukkit] = target.entity

        // Load persisted components
        val pdc = bukkit.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            target.entity.loadComponentsFrom(pdc)

        // allow us to both get the BukkitEntity and specific class (ex Player)
        bukkit.type.entityClass?.kotlin?.let { bukkitClass ->
            target.entity.set(bukkit, bukkitClass)
        }

        target.entity.set(bukkit.uniqueId)
    }
}
