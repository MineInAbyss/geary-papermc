package com.mineinabyss.geary.papermc.bridge.mythicmobs

import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:mythic_skill")
class DoMMSkill(
    val name: String,
)

class DoMMSkillSystem: GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.skill by get<DoMMSkill>().on(source)
    override fun Pointers.handle() {
        MythicBukkit.inst().apiHelper.castSkill(bukkit, skill.name)
    }
}
