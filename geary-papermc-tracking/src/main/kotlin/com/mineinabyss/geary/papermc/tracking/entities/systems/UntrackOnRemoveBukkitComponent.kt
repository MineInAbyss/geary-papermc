package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.addons.dsl.AddonScope
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.Observer
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun AddonScope.createBukkitEntityRemoveListener(): Observer {
    val bukkit2Geary = get<BukkitEntity2Geary>()

    return observe<OnRemove>()
        .involving(query<BukkitEntity>())
        .exec { (bukkit) -> bukkit2Geary.remove(bukkit.entityId) }
}
