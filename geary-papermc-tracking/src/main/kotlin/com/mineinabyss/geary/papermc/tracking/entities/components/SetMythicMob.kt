package com.mineinabyss.geary.papermc.tracking.entities.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.world.entity.EntityType

@JvmInline
@Serializable
@SerialName("geary:set.mythic_mob")
value class SetMythicMob(val id: String)
