package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.commons.events.configurable.components.TriggerWhenSource
import com.mineinabyss.geary.papermc.configlang.helpers.runFollowUp
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope

class TriggerWhenSourceListener : GearyListener() {
    val TargetScope.trigger by getRelations<TriggerWhenSource, Any?>()

    @Handler
    fun TargetScope.tryFollowUpEvents(event: EventScope, source: SourceScope) {
        if (trigger.target.id in event.entity.type) {
            trigger.data.runEvents.runFollowUp(trigger.data.runAsSource, entity, source.entity)
        }
    }
}
