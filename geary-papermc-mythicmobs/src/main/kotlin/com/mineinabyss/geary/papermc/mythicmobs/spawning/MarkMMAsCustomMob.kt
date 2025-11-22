package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.entities.components.ShowInMobQueries
import com.mineinabyss.geary.papermc.tracking.entities.components.SpawnableByGeary
import com.mineinabyss.geary.systems.query.query

fun WorldScoped.markMMAsCustomMob() = observe<OnSet>()
    .involving(query<SetMythicMob>())
    .exec {
        entity.add<ShowInMobQueries>()
        entity.add<SpawnableByGeary>()
    }
