package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.readableString
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlinx.serialization.Serializable

@Serializable(with = InputSerializer::class)
@ExcludeAutoScan
sealed interface Input<T> {
    val type: ComponentId

    class Entities(
        val target: GearyEntity,
        val event: GearyEntity,
    )

    fun get(entities: Entities): T

    @OptIn(UnsafeAccessors::class)
    fun get(pointers: Pointers): T = get(
        Entities(
            pointers.target.entity,
            pointers.event.entity,
        )
    )

    fun evaluate(entities: Entities): Value<T> = Value(type, get(entities))

    class Value<T>(
        override val type: ComponentId,
        val value: T
    ) : Input<T> {
        override fun get(entities: Entities): T = value
    }

    class Derived<T>(
        override val type: ComponentId,
        val readingEntity: GearyEntity
    ) : Input<T> {
        override fun get(entities: Entities): T {
            temporaryEntity { source ->
                source.extend(readingEntity)
                // TODO support set.target
                val target = entities.target
                temporaryEntity { event ->
                    target.callEvent(event, source = source)
                    return event.get(type) as? T
                        ?: error("Failed to get component ${type.readableString()} from event")
                }
            }
        }
    }

    class VariableReference<T>(
        override val type: ComponentId,
        val expression: String
    ) : Input<T> {
        override fun get(entities: Entities): T {
            val foundValue = (entities.event.get<Variables.Evaluated>()
                ?.entries?.get(expression) ?: error("Failed to find variable $expression"))
            check(foundValue.type == type) { "Variable $expression is of type ${foundValue.type.readableString()} but expected ${type.readableString()}" }
            return foundValue.get(entities) as T
        }
    }

    companion object {
        inline fun <reified T> reference(expression: String) =
            VariableReference<T>(componentId<T>(), expression)

        inline fun <reified T> of(value:T) =
                Value(componentId<T>(), value)
    }
}
