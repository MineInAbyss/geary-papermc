package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.SetTarget
import com.mineinabyss.geary.papermc.bridge.config.conditions.Conditions
import com.mineinabyss.geary.papermc.bridge.config.inputs.Variables
import kotlin.reflect.KClass

object EventHelpers {
    inline fun <reified T : Component> defaultTo(): ComponentDefinition {
        return object : ComponentDefinition {
            override fun onCreate(component: GearyEntity) {
                component.addRelation<OnEvent, T>()
            }
        }
    }

    fun <T : Any> runSkill(
        target: GearyEntity,
        initiator: GearyEntity = target,
        type: KClass<T>,
        conditions: (T) -> Boolean = { true },
        onEvent: GearyEntity.() -> Unit = {}
    ) {
        initiator.getRelations(componentId(type), componentId<Any>()).forEach { relation ->
            (initiator.get(relation.id.withRole(HOLDS_DATA)) as? T)?.let(conditions) != false || return@forEach
            temporaryEntity { event ->
                val source = relation.target.toGeary()
                event.onEvent()

                val variables = source.get<Variables>()
                if (variables != null) {
                    event.set(variables)
                }

                val check = source.get<Conditions>()?.check
                fun callEvent(entity: GearyEntity) {
                    check?.let {
                        event.add<RequestCheck>()
                        entity.callEvent(event = event, source = it)
                        if (event.has<FailedCheck>()) return
                        event.remove<RequestCheck>()
                    }
                    entity.callEvent(event = event, source = source)
                }

                val setTarget = source.get<SetTarget>()
                if (setTarget != null) {
                    event.getRelations(setTarget.findByKind, componentId<Any>()).forEach {
                        callEvent(it.target.toGeary())
                    }
                } else callEvent(target)
            }
        }
    }

    inline fun <reified T : Any> runSkill(
        target: GearyEntity,
        initiator: GearyEntity = target,
        noinline conditions: (T) -> Boolean = { true },
        noinline onEvent: GearyEntity.() -> Unit = {}
    ) {
        runSkill(target, initiator, T::class, conditions, onEvent)
    }
}
