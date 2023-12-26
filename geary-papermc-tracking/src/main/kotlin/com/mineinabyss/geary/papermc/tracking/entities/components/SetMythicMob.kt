package com.mineinabyss.geary.papermc.tracking.entities.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:set.mythicMob")
value class SetMythicMob(val id: String)
