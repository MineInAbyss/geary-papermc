package com.mineinabyss.geary.papermc.features.common.event_bridge.items

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import io.papermc.paper.event.player.PlayerArmSwingEvent
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
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
@SerialName("geary:item_right_click_block")
class OnItemRightClickBlock

@Serializable
@SerialName("geary:item_right_click_entity")
class OnItemRightClickEntity

class ItemInteractBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRightClickEntity() = with(player.world.toGeary()) {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemRightClickEntity>()
    }


    @EventHandler
    fun PlayerArmSwingEvent.onLeftClick() = with(player.world.toGeary()) {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemLeftClick>()
    }

    @EventHandler
    fun PlayerInteractEvent.onClick() = with(player.world.toGeary()) {
        if(useItemInHand() == Event.Result.DENY) return
        val heldItem = player.inventory.toGeary()?.get(hand ?: return) ?: return

        // Right click gets fired twice, so we manually prevent two right-clicks within several ticks of each other.

        heldItem.emit<OnItemInteract>()

        if (leftClicked) heldItem.emit<OnItemLeftClickBlock>()
        if (rightClicked) heldItem.emit<OnItemRightClick>()
        if (action == Action.RIGHT_CLICK_BLOCK) heldItem.emit<OnItemRightClickBlock>()
    }
}
