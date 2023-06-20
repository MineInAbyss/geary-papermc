package com.mineinabyss.geary.papermc.bridge.actions.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute

/**
 * @param attribute The attribute to modify.
 * @param amplifier Specifies amount to modify attribute by.
 */
@Serializable
@SerialName("geary:attribute")
data class ApplicableAttribute(
    val attribute: Attribute,
    val amplifier: Double = 1.0
)

