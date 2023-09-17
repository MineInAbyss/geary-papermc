package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRun
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class EventRunListener : GearyListener() {
    val Pointers.run by getRelationsWithData<EventRun?, Any?>().on(event)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val source = source ?: return
        run.forEach { run ->
            target.entity.callEvent(run.target, source = source.entity)
        }
    }
}
