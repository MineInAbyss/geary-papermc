package com.mineinabyss.geary.papermc.bridge.systems

import com.mineinabyss.geary.papermc.bridge.components.*
import com.mineinabyss.geary.papermc.bridge.helpers.setBukkitEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent

class ItemActionsBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler
    fun PlayerInteractEvent.onClick() {
        player.inventory.toGeary()?.itemInMainHand?.callEvent(source = player.toGeary()) {
            add<Interacted>()
            if (leftClicked) add<LeftClicked>()

            // Right click gets fired twice, so we manually prevent two right-clicks within several ticks of each other.
            val currTick = Bukkit.getServer().currentTick
            val eId = player.entityId
            if (rightClicked && currTick - rightClickCooldowns[eId] > 3) {
                rightClickCooldowns[eId] = currTick
                add<RightClicked>()
            }
            setBukkitEvent(this@onClick)
        }
    }

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
