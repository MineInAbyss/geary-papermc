package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRun
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRunBuilder
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class EventRunBuilderToRelation : GearyListener() {
    val TargetScope.run by onSet<EventRunBuilder>()

    @Handler
    fun TargetScope.handle() {
        entity.addRelation<EventRun>(entity.parseEntity(run.expression))
        entity.remove<EventRunBuilder>()
    }
}
