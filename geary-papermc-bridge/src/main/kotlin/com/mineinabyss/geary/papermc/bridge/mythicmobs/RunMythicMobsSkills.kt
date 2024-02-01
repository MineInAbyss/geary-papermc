package com.mineinabyss.geary.papermc.bridge.mythicmobs

import com.google.common.collect.Lists
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMetadataImpl
import io.lumine.mythic.core.skills.SkillTriggers
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.jvm.optionals.getOrNull

@Serializable(with= RunMythicMobsSkills.Serializer::class)
class RunMythicMobsSkills(
    val keys: List<String>,
) {
    class Serializer : InnerSerializer<List<String>, RunMythicMobsSkills>(
        serialName = "geary:run_mythic_skills",
        inner = ListSerializer(String.serializer()),
        inverseTransform = { it.keys },
        transform = ::RunMythicMobsSkills
    )
}

class RunMMSkillSystem: GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.skill by get<RunMythicMobsSkills>().on(source)

    override fun Pointers.handle() {
        skill.keys.forEach {
            val line = "[ - $it ]"
            val entity = BukkitAdapter.adapt(bukkit)
            val caster = MythicBukkit.inst().skillManager.getCaster(entity)
            val skill = MythicBukkit.inst().skillManager.getSkill(line).getOrNull()
            val meta = SkillMetadataImpl(
                SkillTriggers.API,
                caster,
                entity,
                entity.location,
                Lists.newArrayList(*arrayOf<AbstractEntity>(entity)),
                null,
                1.0f
            )
            skill?.execute(meta)
        }
    }
}
