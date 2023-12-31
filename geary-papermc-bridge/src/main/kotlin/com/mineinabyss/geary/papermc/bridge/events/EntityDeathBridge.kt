package com.mineinabyss.geary.papermc.bridge.events

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("geary:on.death")
class OnDeath

class Drops(
    val items: MutableList<ItemStack>,
    var exp: Int,
)

class EntityDeathBridge : Listener {
    @EventHandler
    fun EntityDeathEvent.onDeath() {
        val gearyEntity = entity.toGearyOrNull() ?: return
        val drops = Drops(items = drops, exp = droppedExp)
        EventHelpers.runSkill<OnDeath>(gearyEntity) {
            set(drops)
        }
        droppedExp = drops.exp
    }
}
