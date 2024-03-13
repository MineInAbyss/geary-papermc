package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun GearyModule.createBukkitEntityRemoveListener() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        override fun ensure() = event { has<EntityRemoved>() }
    }
).exec {
    gearyMobs.bukkit2Geary.remove(bukkit.entityId)
}
