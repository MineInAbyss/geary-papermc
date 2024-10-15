package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun Geary.createBukkitEntityRemoveListener() = observe<OnRemove>()
    .involving(query<BukkitEntity>())
    .exec { (bukkit) -> gearyMobs.bukkit2Geary.remove(bukkit.entityId) }
