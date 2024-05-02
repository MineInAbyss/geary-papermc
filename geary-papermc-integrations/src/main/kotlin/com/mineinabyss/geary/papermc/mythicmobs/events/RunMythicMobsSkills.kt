package com.mineinabyss.geary.papermc.mythicmobs.events

import com.google.common.collect.Lists
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
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

@Serializable(with = RunMythicMobsSkills.Serializer::class)
class RunMythicMobsSkills(
    val keys: List<String>,
) {
    class Serializer : InnerSerializer<List<String>, RunMythicMobsSkills>(
        serialName = "geary:run_skills",
        inner = ListSerializer(String.serializer()),
        inverseTransform = { it.keys },
        transform = ::RunMythicMobsSkills
    )
}

fun GearyModule.runMMSkillAction() = observeWithData<RunMythicMobsSkills>()
    .exec(query<BukkitEntity>()) { (bukkit) ->
        event.keys.forEach {
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
