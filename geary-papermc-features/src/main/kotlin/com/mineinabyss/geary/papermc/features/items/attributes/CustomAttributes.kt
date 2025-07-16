package com.mineinabyss.geary.papermc.features.items.attributes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:custom_attributes")
data class CustomAttributes(
    val attributes: Map<String, AttributeValue> = emptyMap()
) {
//    // Helper methods for easy access
//    fun getInt(key: String): Int? = (attributes[key] as? AttributeValue.IntValue)?.value
//    fun getDouble(key: String): Double? = (attributes[key] as? AttributeValue.DoubleValue)?.value
//    fun getString(key: String): String? = (attributes[key] as? AttributeValue.StringValue)?.value
//    fun getBoolean(key: String): Boolean? = (attributes[key] as? AttributeValue.BooleanValue)?.value
//
//    fun setInt(key: String, value: Int): CustomAttributes =
//        copy(attributes = attributes + (key to AttributeValue.IntValue(value)))
//    fun setDouble(key: String, value: Double): CustomAttributes =
//        copy(attributes = attributes + (key to AttributeValue.DoubleValue(value)))
//    fun setString(key: String, value: String): CustomAttributes =
//        copy(attributes = attributes + (key to AttributeValue.StringValue(value)))
//    fun setBoolean(key: String, value: Boolean): CustomAttributes =
//        copy(attributes = attributes + (key to AttributeValue.BooleanValue(value)))
}

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