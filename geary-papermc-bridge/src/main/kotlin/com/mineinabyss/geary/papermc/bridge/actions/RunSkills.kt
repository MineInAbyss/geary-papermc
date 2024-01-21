package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
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

@AutoScan
class RunSkillSystem : GearyListener() {
    private val Pointers.action by get<RunSkills>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        action.skills.forEach { skill ->
            EventHelpers.runSkill(
                target.entity,
                event.entity,
                skill,
            )
        }
    }
}
