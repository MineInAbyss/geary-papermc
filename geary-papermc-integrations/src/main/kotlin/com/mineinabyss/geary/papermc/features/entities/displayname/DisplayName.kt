package com.mineinabyss.geary.papermc.features.entities.displayname

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 */
@JvmInline
@Serializable
@SerialName("mobzy:display_name")
value class DisplayName(val name: String)
