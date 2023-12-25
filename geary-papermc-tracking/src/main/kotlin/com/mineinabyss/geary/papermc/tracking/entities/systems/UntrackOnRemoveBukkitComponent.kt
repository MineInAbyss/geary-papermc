package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity

class UntrackOnRemoveBukkitComponent : GearyListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
    private val Pointers.removed by family { has<EntityRemoved>() }.on(event)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        gearyMobs.bukkit2Geary.remove(bukkit.entityId)
    }
}
