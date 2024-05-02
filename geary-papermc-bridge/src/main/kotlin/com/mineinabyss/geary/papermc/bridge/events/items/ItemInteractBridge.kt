package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

@Serializable
@SerialName("geary:on.item_interact")
sealed class OnItemInteract

@Serializable
@SerialName("geary:on_item_left_click")
sealed class OnItemLeftClick

@Serializable
@SerialName("geary:on_item_right_click")
sealed class OnItemRightClick

@Serializable
@SerialName("geary:on_item_right_click_entity")
sealed class OnItemRightClickEntity

class ItemInteractBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRightClickEntity() {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemRightClickEntity>()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.onClick() {
        val gearyPlayer = player.toGearyOrNull() ?: return
        val heldItem = player.inventory.toGeary()?.get(hand ?: return) ?: return

        // Right click gets fired twice, so we manually prevent two right-clicks within several ticks of each other.
        fun rightClicked(): Boolean {
            val currTick = Bukkit.getServer().currentTick
            val eId = player.entityId
            val cooldownRightClicked = rightClicked && currTick - rightClickCooldowns[eId] > 3
            if (cooldownRightClicked) {
                rightClickCooldowns[eId] = currTick
            }
            return cooldownRightClicked
        }

        heldItem.emit<OnItemInteract>()

        if (leftClicked) heldItem.emit<OnItemLeftClick>()
        if (rightClicked()) heldItem.emit<OnItemRightClick>()
    }
}
