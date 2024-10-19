package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun Geary.createBukkitEntityRemoveListener() = observe<OnRemove>()
    .involving(query<BukkitEntity>())
    .exec { (bukkit) -> getAddon(EntityTracking).bukkit2Geary.remove(bukkit.entityId) }
