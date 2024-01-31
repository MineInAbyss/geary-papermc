package com.mineinabyss.geary.papermc.bridge.mythicmobs

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

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
            MythicBukkit.inst().apiHelper.castSkill(bukkit, it)
        }
    }
}
