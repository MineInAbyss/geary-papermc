package com.mineinabyss.geary.papermc.commons.events.configurable.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.conditions")
class EventConditions(
    val expressions: List<String>
)
