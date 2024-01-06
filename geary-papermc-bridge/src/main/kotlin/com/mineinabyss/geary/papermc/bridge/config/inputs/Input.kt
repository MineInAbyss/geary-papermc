package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.readableString
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlinx.serialization.Serializable

@Serializable(with = InputSerializer::class)
@ExcludeAutoScan
sealed interface Input<T> {
    val type: ComponentId

    fun get(pointers: Pointers): T

    class Value<T : Any>(val value: T) : Input<T> {
        override val type = componentId(value::class)
        override fun get(pointers: Pointers): T = value
    }

    @OptIn(UnsafeAccessors::class)
    class Derived<T>(
        override val type: ComponentId,
        val readingEntity: GearyEntity
    ) : Input<T> {
        override fun get(pointers: Pointers): T {
            temporaryEntity { source ->
                source.extend(readingEntity)
                // TODO support set.target
                val target = pointers.target.entity
                temporaryEntity { event ->
                    target.callEvent(event, source = source)
                    return event.get(type) as? T
                        ?: error("Failed to get component ${type.readableString()} from event")
                }
            }
        }
    }

    @OptIn(UnsafeAccessors::class)
    class VariableReference<T>(
        override val type: ComponentId,
        val expression: String
    ) : Input<T> {
        override fun get(pointers: Pointers): T {
            if (expression.startsWith("lookup")) {
                if(type != componentId(GearyEntity::class)) error("Lookup can only be used with GearyEntity type")
                val lookup = expression.removePrefix("lookup(").removeSuffix(")")
                return pointers.source?.entity?.lookup(lookup) as? T ?: error("Failed to lookup entity: $lookup")
            }
            val foundValue = (pointers.event.entity.get<Variables>()
                ?.entries?.get(expression) ?: error("Failed to find variable $expression"))
            check(foundValue.type == type) { "Variable $expression is of type ${foundValue.type.readableString()} but expected ${type.readableString()}" }
            return foundValue.get(pointers) as T
        }
    }

}
