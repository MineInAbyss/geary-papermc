package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class InPlaceInput(
    @SerialName("\$derived")
    val input: SerializableGearyEntity
)
