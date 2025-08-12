package com.mineinabyss.geary.papermc.features.items.attributes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:custom_attributes")
data class CustomAttributes(
    val attributes: Map<String, AttributeValue> = emptyMap()
)

@Serializable
sealed class AttributeValue {
    @Serializable
    @SerialName("int")
    data class IntValue(val value: Int) : AttributeValue()

    @Serializable
    @SerialName("double")
    data class DoubleValue(val value: Double) : AttributeValue()

    @Serializable
    @SerialName("string")
    data class StringValue(val value: String) : AttributeValue()

    @Serializable
    @SerialName("boolean")
    data class BooleanValue(val value: Boolean) : AttributeValue()
}