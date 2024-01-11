package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = Subskills.Serializer::class)
class Subskills(
    val skills: List<Skill>,
) {
    class Serializer : InnerSerializer<List<Skill>, Subskills>(
        "geary:subskills",
        ListSerializer(Skill.serializer()),
        { Subskills(it) },
        { it.skills },
    )
}

