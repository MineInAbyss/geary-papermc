package com.mineinabyss.geary.papermc.features.items.attributes

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("geary:attribute_condition")
class AttributeCondition(
    val name: String,
    val comparison: AttributeComparison = AttributeComparison.GREATER_THAN,
    val value: AttributeValue
) : Condition {
    @Transient
    private var lastComp: Boolean = false

    override fun ActionGroupContext.execute(): Boolean {
        val attributes = entity?.get<CustomAttributes>() ?: return false
        val currentValue = attributes.attributes[name] ?: return false


        val result = when (comparison) {
            AttributeComparison.EQUALS -> {
                currentValue == value
            }

            AttributeComparison.GREATER_THAN -> {
                if (currentValue is AttributeValue.IntValue && value is AttributeValue.IntValue) {
                    currentValue.value > value.value
                } else if (currentValue is AttributeValue.DoubleValue && value is AttributeValue.DoubleValue) {
                    currentValue.value > value.value
                } else {
                    false
                }
            }

            AttributeComparison.LESS_THAN -> {
                if (currentValue is AttributeValue.IntValue && value is AttributeValue.IntValue) {
                    currentValue.value < value.value
                } else if (currentValue is AttributeValue.DoubleValue && value is AttributeValue.DoubleValue) {
                    currentValue.value < value.value
                } else {
                    false
                }
            }
        }

        /*
        Bandaid fix to account for the fact that conditions are executed twice.
        This allows to prevent the "onFail" block from being executed in case
        the condition succeeds the first time.
         */
        if (result) {
            lastComp = true
            return true
        }

        if (lastComp) {
            lastComp = false
            return true
        }

        return false
    }
}

@Serializable
enum class AttributeComparison {
    EQUALS, GREATER_THAN, LESS_THAN
}