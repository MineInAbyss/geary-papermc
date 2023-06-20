package com.mineinabyss.geary.papermc.bridge.conditions.location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:check.time")
class TimeCondition(
    //TODO change to range
    val min: Long = -1,
    val max: Long = 10000000,
)

