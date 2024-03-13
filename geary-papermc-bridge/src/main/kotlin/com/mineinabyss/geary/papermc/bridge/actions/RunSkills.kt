package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = RunSkills.Serializer::class)
class RunSkills(
    val keys: List<PrefabKey>
) {
    @Transient
    val skills = keys.map { key ->
        prefabs.manager[key]?.get<Skill>() ?: error("Could not find skill $key")
    }

    class Serializer : InnerSerializer<List<PrefabKey>, RunSkills>(
        serialName = "geary:run_skills",
        inner = ListSerializer(PrefabKeySerializer),
        inverseTransform = { it.keys },
        transform = ::RunSkills
    )
}

fun GearyModule.createRunSkillAction() = listener(
    object : ListenerQuery() {
        val action by source.get<RunSkills>()
    }
).exec {
    action.skills.forEach { skill ->
        EventHelpers.runSkill(
            entity,
            event.entity,
            skill,
        )
    }
}
