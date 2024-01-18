package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("geary:spawn")
class RunSkill(
    val key: PrefabKey
) {
    @Transient
    val skill = prefabs.manager[key]?.get<Skill>() ?: error("Could not find skill $key")
}

@AutoScan
class RunSkillSystem : GearyListener() {
    private val Pointers.action by get<RunSkill>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        EventHelpers.runSkill(
            target.entity,
            event.entity,
            action.skill,
        )
    }
}
