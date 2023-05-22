package com.mineinabyss.geary.papermc.commons.events.configurable.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.triggers")
class EventTriggers(
    val expressions: List<String>
)

