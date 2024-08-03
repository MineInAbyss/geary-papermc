package com.mineinabyss.geary.papermc.mythicmobs.skills

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicSkillRegisterListener: Listener {
    @EventHandler
    fun MythicMechanicLoadEvent.onMechanicLoad() {
        when(mechanicName.lowercase()) {
            "prefabs" -> register(PrefabsMechanic(config))
        }
    }
}
