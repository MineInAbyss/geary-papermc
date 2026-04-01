package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.kodein.di.instance

fun WorldScoped.createBukkitEntityRemoveListener(
    bukkit2Geary: BukkitEntity2Geary = instance(),
): Observer {
    return observe<OnRemove>()
        .involving(query<BukkitEntity>())
        .exec { (bukkit) -> bukkit2Geary.remove(bukkit.entityId) }
}
