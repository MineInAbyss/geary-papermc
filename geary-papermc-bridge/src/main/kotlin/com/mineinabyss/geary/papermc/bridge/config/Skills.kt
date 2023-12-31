package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer


@Serializable(with = Skills.Serializer::class)
class Skills(
    val skills: List<Skill>,
) {
    class Serializer : InnerSerializer<List<Skill>, Skills>(
        "geary:skills",
        ListSerializer(Skill.serializer()),
        { Skills(it) },
        { it.skills },
    )
}

