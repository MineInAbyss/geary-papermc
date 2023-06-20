package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.papermc.bridge.components.Dead
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class DeathBridge : Listener {
    @EventHandler
    fun EntityDeathEvent.addDeadComponent() {
        entity.toGeary().add<Dead>()
    }
}
