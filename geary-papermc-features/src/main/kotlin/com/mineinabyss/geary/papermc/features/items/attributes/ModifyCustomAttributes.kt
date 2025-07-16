package com.mineinabyss.geary.papermc.features.items.attributes

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:modify_attribute")
class ModifyAttributeAction(
    val name: String,
    val operation: AttributeOperation,
    val value: AttributeValue
) : Action {
    override fun ActionGroupContext.execute() {
        val current = entity?.get<CustomAttributes>() ?: CustomAttributes()
        val currentValue = current.attributes[name]

        val newValue = when (operation) {
            AttributeOperation.ADD -> when {
                currentValue is AttributeValue.IntValue && value is AttributeValue.IntValue ->
                    AttributeValue.IntValue(currentValue.value + value.value)

                currentValue is AttributeValue.DoubleValue && value is AttributeValue.DoubleValue ->
                    AttributeValue.DoubleValue(currentValue.value + value.value)

                else -> value
            }

            AttributeOperation.MULTIPLY -> when {
                currentValue is AttributeValue.IntValue && value is AttributeValue.IntValue ->
                    AttributeValue.IntValue(currentValue.value * value.value)

                currentValue is AttributeValue.DoubleValue && value is AttributeValue.DoubleValue ->
                    AttributeValue.DoubleValue(currentValue.value * value.value)

                else -> value
            }

            AttributeOperation.SET -> value
        }

        val updated = current.copy(
            attributes = current.attributes + (name to newValue)
        )
        entity?.set(updated)
    }
}

@Serializable
enum class AttributeOperation {
    SET, ADD, MULTIPLY
}