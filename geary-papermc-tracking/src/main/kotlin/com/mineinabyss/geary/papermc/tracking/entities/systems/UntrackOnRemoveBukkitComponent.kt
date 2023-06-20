package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.typealiases.BukkitEntity

class UntrackOnRemoveBukkitComponent : GearyListener() {
    private val TargetScope.bukkit by get<BukkitEntity>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.persistComponents() {
        gearyMobs.bukkit2Geary.remove(bukkit.entityId)
        entity.encodeComponentsTo(bukkit)
    }
}
