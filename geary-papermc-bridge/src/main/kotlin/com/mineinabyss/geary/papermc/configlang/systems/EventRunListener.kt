package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRun
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope

class EventRunListener : GearyListener() {
    val EventScope.run by getRelations<EventRun?, Any?>()

    @Handler
    fun handle(source: SourceScope, target: TargetScope, event: EventScope) {
        target.entity.callEvent(event.run.target, source = source.entity)
    }
}
