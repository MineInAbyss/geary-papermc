package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventCondition
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventConditions
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class ConditionsToRoles : GearyListener() {
    val TargetScope.triggers by onSet<EventConditions>()

    @Handler
    fun TargetScope.convert() {
        try {
            triggers.expressions.forEach { expression ->
                val (cause, condition, effect) = expression.replace(" ", "").split("->").takeIf { it.size == 3 }
                    ?: error("Expression needs to be formatted as 'cause -> condition -> effect'")
                cause == "any" || error("Only 'any' is currently supported as a cause.")
                entity.setRelation(
                    EventCondition(entity = entity.parseEntity(condition).id),
                    entity.parseEntity(effect)
                )
            }
        } finally {
            entity.remove<EventConditions>()
        }
    }
}
