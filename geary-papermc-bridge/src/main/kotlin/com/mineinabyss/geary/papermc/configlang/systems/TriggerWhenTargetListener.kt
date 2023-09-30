package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.configlang.components.TriggerWhenTarget
import com.mineinabyss.geary.papermc.configlang.helpers.runFollowUp
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class TriggerWhenTargetListener : GearyListener() {
    val Pointers.triggerRelations by getRelationsWithData<TriggerWhenTarget, Any?>().on(target)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val source = source ?: return
        // If event has our trigger
        triggerRelations.forEach { trigger ->
            if (trigger.target.id in event.entity.type) {
                trigger.data.runEvents.runFollowUp(trigger.data.runAsSource, target.entity, source.entity)
            }
        }
    }
}
