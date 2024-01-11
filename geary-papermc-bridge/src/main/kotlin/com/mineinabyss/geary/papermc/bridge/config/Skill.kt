package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import kotlinx.serialization.Serializable

@Serializable(with = Skill.Serializer::class)
class Skill(val entity: SerializableGearyEntity) {
    class Serializer : InnerSerializer<SerializableGearyEntity, Skill>(
        "geary:skill",
        GearyEntitySerializer,
        { Skill(it) },
        { it.entity },
    )
}
