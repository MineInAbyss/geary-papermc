package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import kotlin.reflect.KClass

object EventHelpers {
    inline fun <reified T: Component> defaultTo(): ComponentDefinition {
        return object : ComponentDefinition {
            override fun onCreate(component: GearyEntity) {
                component.addRelation<OnEvent, T>()
            }
        }
    }

    fun <T: Any> runSkill(
        target: GearyEntity,
        type: KClass<T>,
        conditions: (T) -> Boolean = { true },
        onEvent: GearyEntity.() -> Unit = {}
    ) {
        target.getRelations(componentId(type), componentId<Any>()).forEach { relation ->
            (target.get(relation.id) as? T)?.let(conditions) != false || return@forEach
            temporaryEntity { event ->
                temporaryEntity { source ->
                    event.onEvent()
                    source.extend(relation.target.toGeary())
                    target.callEvent(event = event, source = source)
                }
            }
        }
    }

    inline fun <reified T: Any> runSkill(
        target: GearyEntity,
        noinline conditions: (T) -> Boolean = { true },
        noinline onEvent: GearyEntity.() -> Unit = {}
    ) =
        runSkill(target, T::class, conditions, onEvent)
}
