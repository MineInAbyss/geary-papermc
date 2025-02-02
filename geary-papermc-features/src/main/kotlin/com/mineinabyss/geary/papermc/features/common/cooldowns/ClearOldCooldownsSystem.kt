package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query

/**
 * Clear cooldowns that have expired from [Cooldowns] component.
 */
fun Geary.clearOldCooldownsSystem() = system(query<Cooldowns>())
    .every(CooldownDisplayProps.CLEAR_OLD_COOLDOWNS_INTERVAL)
    .execOnAll {
        mapNotNullWithEntity { (cooldowns) ->
            if (cooldowns.cooldowns.all { !it.value.isComplete() }) return@mapNotNullWithEntity null
            else Cooldowns(cooldowns.cooldowns.filterValues { !it.isComplete() })
        }.fastForEach { (filteredCooldowns, entity) ->
            if (filteredCooldowns.cooldowns.isEmpty()) entity.remove<Cooldowns>()
            else entity.setPersisting(filteredCooldowns)
        }
    }
