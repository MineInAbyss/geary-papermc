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
        runOn.forEach { chosenTarget ->
            // All variables are evaluated upon event call
            val appendVariables = skill.vars
                ?.fold(event.get<Variables.Evaluated>() ?: Variables.Evaluated()) { acc, variables ->
                    val inputTarget = (variables.using?.let { findTargets(target, event, it) } ?: listOf(chosenTarget))
                        .firstOrNull() ?: error("Must have exactly one target to read variables from")
                    acc + variables.evaluated(Input.Entities(inputTarget, event))
                }
            if (appendVariables != null) event.set(appendVariables)

            // Run condition checks
            val conditions = skill.conditions
            if (conditions != null) {
                event.add<RequestCheck>()
                conditions.forEach {
                    chosenTarget.callEvent(event = event, source = it)
                    if (event.has<FailedCheck>()) {
                        skill.onFail?.skills?.forEach { subskill ->
                            runSkill(chosenTarget, event, subskill)
                        }
                        return@runSkill
                    }
                }
                event.remove<RequestCheck>()
            }

            skill.run?.skills?.forEach { subskill ->
                runSkill(chosenTarget, event, subskill)
            }
            if (skill.execute != null) {
                chosenTarget.callEvent(event = event, source = skill.execute)
            }
        }
    }

    fun runSkill(target: GearyEntity, skill: Skill) {
        temporaryEntity { event ->
            runSkill(target, event, skill)
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
