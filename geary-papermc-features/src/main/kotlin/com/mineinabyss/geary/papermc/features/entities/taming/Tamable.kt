package com.mineinabyss.geary.papermc.features.entities.taming

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:tamable")
class Tamable(
    val tameItem: SerializableItemStack? = null,
    val saddleModelId: String? = null,
)
