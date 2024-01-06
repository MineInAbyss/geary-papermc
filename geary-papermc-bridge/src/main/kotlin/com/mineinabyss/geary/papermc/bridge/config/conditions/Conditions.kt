package com.mineinabyss.geary.papermc.bridge.config.conditions

import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import kotlinx.serialization.Serializable

@Serializable(with = Conditions.Serializer::class)
class Conditions(
    val check: SerializableGearyEntity,
) {
    class Serializer : InnerSerializer<SerializableGearyEntity, Conditions>(
        "geary:conditions",
        GearyEntitySerializer,
        { Conditions(it) },
        { it.check },
    )
}
