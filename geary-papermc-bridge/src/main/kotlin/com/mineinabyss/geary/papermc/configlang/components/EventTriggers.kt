package com.mineinabyss.geary.papermc.commons.events.configurable.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:event.triggers")
value class EventTriggers(
    val expressions: List<String>
)

