package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.prefabs.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable

@Serializable(with = Skill.Serializer::class)
class Skill(val run: List<GearyComponent>) {
    val eventComponent: EventComponent = (run
        .firstOrNull { component -> component is EventComponent }
        ?: error("Skill must contain an event component")) as EventComponent

    class Serializer : InnerSerializer<List<GearyComponent>, Skill>(
        "geary:skill",
        PolymorphicListAsMapSerializer.of(PolymorphicSerializer(GearyComponent::class)),
        { Skill(it) },
        { it.run },
    )
}
