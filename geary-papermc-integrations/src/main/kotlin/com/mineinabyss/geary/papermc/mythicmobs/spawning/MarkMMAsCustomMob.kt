package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.tracking.entities.components.ShowInMobQueries
import com.mineinabyss.geary.papermc.tracking.entities.components.SpawnableByGeary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query

fun GearyModule.markMMAsCustomMob() = observe<OnSet>()
    .involving(query<SetMythicMob>())
    .exec {
        entity.add<ShowInMobQueries>()
        entity.add<SpawnableByGeary>()
    }

fun GearyModule.markBindMMAsCustomMob() = observe<OnSet>()
    .involving(query<BindToMythicMob>())
    .exec { entity.add<ShowInMobQueries>() }
