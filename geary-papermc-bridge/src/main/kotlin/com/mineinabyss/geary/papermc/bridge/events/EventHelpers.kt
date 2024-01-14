package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.SetTarget
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
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

    fun findTargets(currentTarget: GearyEntity, event: GearyEntity, using: SetTarget): List<GearyEntity> {
        val data = using.inner.data
        if (data == null)
            return event.getRelations(using.inner.type, componentId<Any>()).map { it.target.toGeary() }
        else {
            currentTarget.callEvent(event, using.readerEntity)
            val targets = event.get<EmittedTargets>() ?: return emptyList()
            event.remove<EmittedTargets>()
            return targets.targets
        }
    }

    fun runSkill(target: GearyEntity, event: GearyEntity, skill: Skill) {
        val runOn = skill.using?.let { findTargets(target, event, it) } ?: listOf(target)

        // All variables are evaluated upon event call
        val appendVariables = skill.vars
        if (appendVariables != null) {
            val variables = event.get<Variables>()
            val evaluatedVariables = appendVariables.evaluated(Input.Entities(target, event, skill))
            if (variables == null) event.set(evaluatedVariables)
            else event.set(variables.plus(evaluatedVariables))
        }

        // Run condition checks
        val conditions = skill.conditions
        if (conditions != null) {
            event.add<RequestCheck>()
            conditions.forEach {
                target.callEvent(event = event, source = it)
                if (event.has<FailedCheck>()) return
            }
            event.remove<RequestCheck>()
        }

        runOn.forEach { chosenTarget ->
            skill.run?.skills?.forEach { subskill ->
                runSkill(chosenTarget, event, subskill)
            }
            if (skill.execute != null) {
                chosenTarget.callEvent(event = event, source = skill.execute)
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
                runSkill(target, event, source.get<Skill>() ?: return@forEach)
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
