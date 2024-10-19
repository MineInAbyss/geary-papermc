package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.entities.components.ShowInMobQueries
import com.mineinabyss.geary.papermc.tracking.entities.components.SpawnableByGeary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun Geary.markMMAsCustomMob() = observe<OnSet>()
    .involving(query<SetMythicMob>())
    .exec {
        entity.add<ShowInMobQueries>()
        entity.add<SpawnableByGeary>()
    }
