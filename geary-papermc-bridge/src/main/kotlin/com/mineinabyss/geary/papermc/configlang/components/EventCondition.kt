package com.mineinabyss.geary.papermc.commons.events.configurable.components

import com.mineinabyss.geary.datatypes.GearyEntityId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.condition")
class EventCondition(
    val entity: GearyEntityId
)
