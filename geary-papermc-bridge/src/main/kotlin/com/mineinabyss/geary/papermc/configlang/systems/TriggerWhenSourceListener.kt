package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.configlang.components.TriggerWhenSource
import com.mineinabyss.geary.papermc.configlang.helpers.runFollowUp
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class TriggerWhenSourceListener : GearyListener() {
    val Pointers.triggerRelations by getRelationsWithData<TriggerWhenSource, Any?>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val source = source ?: return
        triggerRelations.forEach { trigger ->
            if (trigger.target.id in event.entity.type) {
                trigger.data.runEvents.runFollowUp(trigger.data.runAsSource, target.entity, source.entity)
            }
        }
    }
}
