package com.mineinabyss.geary.papermc.features.common.event_bridge.items

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.GearyPlayerInventory
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
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

@Serializable
@SerialName("geary:item_interact")
class OnItemInteract

@Serializable
@SerialName("geary:item_left_click")
class OnItemLeftClick

@Serializable
@SerialName("geary:item_left_click_sneak")
class OnItemLeftClickSneak

@Serializable
@SerialName("geary:item_left_click_block")
class OnItemLeftClickBlock

@Serializable
@SerialName("geary:item_right_click")
class OnItemRightClick

@Serializable
@SerialName("geary:item_right_click_sneak")
class OnItemRightClickSneak

@Serializable
@SerialName("geary:item_right_click_block")
class OnItemRightClickBlock

@Serializable
@SerialName("geary:item_right_click_entity")
class OnItemRightClickEntity

@Serializable
@SerialName("geary:item_swap_in")
class OnItemSwapIn

@Serializable
@SerialName("geary:item_swap_out")
class OnItemSwapOut

@Serializable
@SerialName("geary:item_sneak")
class OnItemSneak



class ItemInteractBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onRightClickEntity() = with(player.world.toGeary()) {
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        heldItem.emit<OnItemRightClickEntity>()
    }

    private inline fun <reified E : Any>emitToAll(inventory: GearyPlayerInventory?) {
        // Armor Slots
        inventory?.itemInHelmet?.emit<E>()
        inventory?.itemInChestplate?.emit<E>()
        inventory?.itemInLeggings?.emit<E>()
        inventory?.itemInBoots?.emit<E>()

        // Passive Slots
        inventory?.get(9)?.emit<E>()
        inventory?.get(10)?.emit<E>()
    }

    @EventHandler
    fun PlayerArmSwingEvent.onLeftClick() {
        val heldItem = player.inventory.toGeary()?.get(hand)
        heldItem?.emit<OnItemLeftClick>()
        emitToAll<OnItemLeftClick>(player.inventory.toGeary())
        if (player.isSneaking) {
            emitToAll<OnItemLeftClickSneak>(player.inventory.toGeary())
            heldItem?.emit<OnItemLeftClick>()
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onClick() {
        // Don't fire if item is used
        if (useItemInHand() == Event.Result.DENY) return

        val heldItem = hand?.let { player.inventory.toGeary()?.get(it) }

        // Right click gets fired twice, so we manually prevent two right-clicks within several ticks of each other.
        heldItem?.emit<OnItemInteract>()

        if (leftClicked) {
            heldItem?.emit<OnItemLeftClickBlock>()
            if (player.isSneaking) {
                heldItem?.emit<OnItemLeftClickSneak>()
            }
        }

        if (rightClicked) {
            heldItem?.emit<OnItemRightClick>()
            emitToAll<OnItemRightClick>(player.inventory.toGeary())
            if (player.isSneaking) {
                heldItem?.emit<OnItemRightClickSneak>()
                emitToAll<OnItemRightClickSneak>(player.inventory.toGeary())
            }
        }
        if (action == Action.RIGHT_CLICK_BLOCK) heldItem?.emit<OnItemRightClickBlock>()
    }

    @EventHandler
    fun PlayerItemHeldEvent.onItemSwap() {
        val inventory = player.inventory.toGeary()
        inventory?.get(newSlot)?.emit<OnItemSwapIn>()
        inventory?.get(previousSlot)?.emit<OnItemSwapOut>()
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.onItemSwap() {
        val inventory = player.inventory.toGeary()
        val mainHand = inventory?.itemInMainHand
        val offHand = inventory?.itemInOffhand

        // Emit both swaps for the items

        mainHand?.emit<OnItemSwapIn>()
        mainHand?.emit<OnItemSwapOut>()
        offHand?.emit<OnItemSwapIn>()
        offHand?.emit<OnItemSwapOut>()
    }

    @EventHandler
    fun PlayerToggleSneakEvent.onItemSneak() {
        if (!player.isSneaking) {
            emitToAll<OnItemSneak>(player.inventory.toGeary())
        }
    }
}
