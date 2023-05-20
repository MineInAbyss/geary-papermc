package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventTriggers
import com.mineinabyss.geary.papermc.commons.events.configurable.components.TriggerWhenSource
import com.mineinabyss.geary.papermc.commons.events.configurable.components.TriggerWhenTarget
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class TriggersToRoles : GearyListener() {
    val TargetScope.conditions by onFirstSet<EventTriggers>()

    @Handler
    fun TargetScope.convert() {
        try {
            conditions.expressions.forEach { expression ->
                val (cause, effect) = expression.split(" ?-> ?".toRegex()).takeIf { it.size == 2 }
                    ?: error("Expression $expression needs to be formatted as 'cause -> effect'")
                fun String.action() = replace(" ?(other|this) ?".toRegex(), "")
                fun String.isSource() =
                    matches("this \\S+( other)?".toRegex()).also {
                        if (!it && !matches("(other )?\\S+ this".toRegex()))
                            error("$this should be formatted 'this action other', or 'other action this'")
                    }

                val triggerWhenOnSource = cause.isSource()
                val runAsSource = effect.isSource()

                val causeEntity = entity.parseEntity(cause.action())
                val effectEntities = GearyEntityType(effect.action()
                    .split(", ?".toRegex())
                    .mapTo(mutableSetOf()) { entity.parseEntity(it).id })

                if (triggerWhenOnSource) {
                    val existing = entity.getRelation<TriggerWhenSource>(causeEntity)
                    if (existing != null) entity.setRelation(
                        existing.copy(runEvents = existing.runEvents.plus(effectEntities)),
                        causeEntity,
                    )
                    else entity.setRelation(TriggerWhenSource(runEvents = effectEntities, runAsSource), causeEntity)
                } else {
                    val existing = entity.getRelation<TriggerWhenTarget>(causeEntity)
                    if (existing != null) entity.setRelation(
                        existing.copy(runEvents = existing.runEvents.plus(effectEntities)),
                        causeEntity
                    )
                    else entity.setRelation(TriggerWhenTarget(runEvents = effectEntities, runAsSource), causeEntity)
                }
            }
        } finally {
            entity.remove<EventTriggers>()
        }
    }
}
