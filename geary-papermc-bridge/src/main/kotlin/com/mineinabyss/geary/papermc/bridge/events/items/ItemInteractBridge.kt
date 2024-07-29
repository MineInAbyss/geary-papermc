package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import io.papermc.paper.event.player.PlayerArmSwingEvent
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

@Serializable
@SerialName("geary:item_interact")
class OnItemInteract

@Serializable
@SerialName("geary:item_left_click")
class OnItemLeftClick

@Serializable
@SerialName("geary:item_left_click_block")
class OnItemLeftClickBlock


@Serializable
@SerialName("geary:item_right_click")
class OnItemRightClick

@Serializable
@SerialName("geary:item_right_click_entity")
class OnItemRightClickEntity

class ItemInteractBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRightClickEntity() {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemRightClickEntity>()
    }


    @EventHandler
    fun PlayerArmSwingEvent.onLeftClick() {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemLeftClick>()
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

        if (leftClicked) heldItem.emit<OnItemLeftClickBlock>()
        if (rightClicked()) heldItem.emit<OnItemRightClick>()
    }
}
