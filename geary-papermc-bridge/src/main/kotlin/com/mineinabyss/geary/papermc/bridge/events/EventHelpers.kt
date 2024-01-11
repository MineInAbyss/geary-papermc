package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.SetTarget
import com.mineinabyss.geary.papermc.bridge.config.Subskills
import com.mineinabyss.geary.papermc.bridge.config.conditions.Conditions
import com.mineinabyss.geary.papermc.bridge.config.inputs.Variables
import com.mineinabyss.geary.papermc.bridge.targetselectors.EmittedTargets
import kotlin.reflect.KClass

object EventHelpers {
    inline fun <reified T : Component> defaultTo(): ComponentDefinition {
        return object : ComponentDefinition {
            override fun onCreate(component: GearyEntity) {
                component.addRelation<OnEvent, T>()
            }
        }
    }

    private fun callEventWithOriginalTarget(target: GearyEntity, event: GearyEntity, skill: GearyEntity) {
        val setTarget = skill.get<SetTarget>()
        if (setTarget != null) {
            val data = setTarget.inner.data
            if (data == null)
                event.getRelations(setTarget.inner.type, componentId<Any>()).forEach {
                    callEvent(it.target.toGeary(), event, skill)
                }
            else {
                target.callEvent(event, setTarget.readerEntity)
                val targets = event.get<EmittedTargets>() ?: return
                event.remove<EmittedTargets>()
                targets.targets.forEach { callEvent(it, event, skill) }
            }
        } else {
            callEvent(target, event, skill)
        }
    }

    private fun callEvent(target: GearyEntity, event: GearyEntity, skill: GearyEntity) {
        // All variables are evaluated upon event call
        val appendVariables = skill.get<Variables>()
        if (appendVariables != null) {
            val variables = event.get<Variables>()
            val evaluatedVariables = appendVariables.evaluated(Variables.Entities(target, event, skill))
            if (variables == null) event.set(evaluatedVariables)
            else event.set(variables.plus(evaluatedVariables))
        }

        val check = skill.get<Conditions>()?.check
        check?.let {
            event.add<RequestCheck>()
            target.callEvent(event = event, source = it)
            if (event.has<FailedCheck>()) return
            event.remove<RequestCheck>()
        }

        target.callEvent(event = event, source = skill)
        skill.get<Subskills>()?.skills?.forEach { subSkill ->
            temporaryEntity { subEvent ->
                subEvent.extend(event)
                callEventWithOriginalTarget(target, subEvent, subSkill.entity)
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
                callEventWithOriginalTarget(target, event, source)
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
