package com.mineinabyss.geary.papermc.features.entities.prevent.regen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.entity.EntityRegainHealthEvent

@Serializable
@SerialName("geary:prevent.regeneration")
class PreventRegeneration(val reason: Set<EntityRegainHealthEvent.RegainReason> = setOf())
