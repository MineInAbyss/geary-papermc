package com.mineinabyss.geary.papermc.features.items.nointeraction

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot

class DisableItemInteractionsListener : Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onClick() {
        disableIfNeeded(player, hand)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onClick() {
        disableIfNeeded(player, hand)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerItemConsumeEvent.onConsume() {
        disableIfNeeded(player, hand)
    }

    fun Cancellable.disableIfNeeded(player: Player, hand: EquipmentSlot?) {
        val heldItem = player.inventory.toGeary()?.get(hand ?: return) ?: return
        if (heldItem.has<DisableItemInteractions>())
            isCancelled = true
    }
}
