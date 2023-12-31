package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.bridge.components.*
import com.mineinabyss.geary.papermc.bridge.helpers.setBukkitEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

class ItemActionsBridge : Listener {

    @EventHandler(ignoreCancelled = true)
    fun PlayerItemBreakEvent.onItemBreak() {
        player.inventory.toGeary()?.itemInMainHand?.callEvent {
            add<ItemBroke>()
            setBukkitEvent(this@onItemBreak)
        }
    }

    //TODO dropping items reloads them in the tracking system even if cancelled
    @EventHandler(ignoreCancelled = true)
    fun PlayerDropItemEvent.onItemDrop() {
        player.inventory.toGeary()?.itemInMainHand?.callEvent(source = player.toGeary()) {
            add<ItemDropped>()
            setBukkitEvent(this@onItemDrop)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onHit() {
        val player = damager as? Player ?: return

        //TODO perhaps an event that triggers item -> player, then player -> target
        entity.toGeary().callEvent(source = player.inventory.toGeary()?.itemInMainHand) {
            add<Interacted>()
            add<Attacked>()
            setBukkitEvent(this@onHit)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerItemConsumeEvent.onConsume() {
        player.inventory.toGeary()?.itemInMainHand?.callEvent(source = player.toGeary()) {
            add<Ingested>()
            add<Touched>()
            setBukkitEvent(this@onConsume)
        }
    }
}
