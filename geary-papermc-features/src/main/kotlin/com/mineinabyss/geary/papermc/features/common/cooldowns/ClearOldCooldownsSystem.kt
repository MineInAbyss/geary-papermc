package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.query

fun GearyModule.clearOldCooldownsSystem() = system(query<Cooldowns>()).every(CooldownDisplayProps.INTERVAL).defer { (cooldowns) ->
    Cooldowns(cooldowns.cooldowns.filterValues { !it.isComplete() })
}.onFinish { data, entity ->
    entity.setPersisting(data)
}
