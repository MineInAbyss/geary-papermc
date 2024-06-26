package com.mineinabyss.geary.papermc.features.entities.taming

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("geary:tamed")
class Tamed(
    @Serializable(with = UUIDSerializer::class)
    val owner: UUID,
)
