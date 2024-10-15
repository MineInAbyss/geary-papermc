package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query

fun Geary.clearOldCooldownsSystem() = system(query<Cooldowns>())
    .every(CooldownDisplayProps.INTERVAL)
    .defer { (cooldowns) -> Cooldowns(cooldowns.cooldowns.filterValues { !it.isComplete() }) }
    .onFinish { data, entity ->
        entity.setPersisting(data)
    }
