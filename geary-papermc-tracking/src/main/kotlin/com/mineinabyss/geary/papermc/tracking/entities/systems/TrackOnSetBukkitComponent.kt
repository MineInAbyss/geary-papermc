package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.addons.dsl.AddonScope
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun AddonScope.createBukkitEntitySetListener(): Observer {
    val bukkit2Geary = get<BukkitEntity2Geary>()

    return observe<OnSet>()
        .involving(query<BukkitEntity>())
        .exec { (bukkit) ->
            bukkit2Geary[bukkit] = entity

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
