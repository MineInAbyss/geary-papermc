package com.mineinabyss.geary.papermc.mythicmobs.spawning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("mobzy:set.mythic_mob")
value class SetMythicMob(val id: String)
