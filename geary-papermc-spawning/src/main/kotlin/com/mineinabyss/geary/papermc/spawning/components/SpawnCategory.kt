package com.mineinabyss.geary.papermc.spawning.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:spawn_category")
@JvmInline
value class SpawnCategory(val category: String)
