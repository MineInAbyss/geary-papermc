package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRun
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventRunBuilder
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class EventRunBuilderToRelation : GearyListener() {
    var Pointers.run by get<EventRunBuilder>().removable().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        target.entity.addRelation<EventRun>(target.entity.parseEntity(run!!.expression))
        run = null
    }
}
