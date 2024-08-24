package com.mineinabyss.geary.papermc.features.items.wearables

import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class WearableItemListener : Listener {
    @EventHandler
    fun InventoryClickEvent.shiftClickToWear() {
        if (!click.isShiftClick) return
        if (inventory !is CraftInventoryCrafting) return // Only support shift clicking when only player inventory is open
        val player = inventory.holder as? Player ?: return
        player.inventory.toGeary()?.get(slot)?.with { _: Hat ->
            if (player.inventory.helmet == null) {
                player.inventory.helmet = currentItem
                currentItem = null
                isCancelled = true
            }
        }
    }

    @EventHandler
    fun InventoryClickEvent.clickToWear() {
        // Check that helmet slot was clicked
        if (slotType !== InventoryType.SlotType.ARMOR) return
        if (rawSlot != 5) return

        val player = inventory.holder as? Player ?: return
        val entity = player.inventory.toGeary()?.itemOnCursor ?: return
        val hat = entity.get<Hat>() ?: return


        // swap the items from cursor to helmet slot
        val currItem = currentItem
        currentItem = cursor
        view.setCursor(currItem)
        isCancelled = true
        player.playSound(player.location, hat.sound, 1f, 1f)
    }

    @EventHandler
    fun PlayerInteractEvent.rightClickToWear() {
        if (hand == EquipmentSlot.OFF_HAND) return //the event is called twice, on for each hand. We want to ignore the offhand call
        if (!rightClicked) return //only do stuff when player rightclicks
        if (player.inventory.helmet !== null) return // don't equip if we are wearing a helmet

        val entityInMainHand = player.inventory.toGeary()?.itemInMainHand ?: return
        val hat = entityInMainHand.get<Hat>() ?: return

        player.inventory.helmet = player.inventory.itemInMainHand
        player.inventory.setItemInMainHand(null)
        player.playSound(player.location, hat.sound, 1f, 1f)
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.rightClickArmorStand() {
        val item = player.inventory.itemInMainHand
        val armorstand = rightClicked as? ArmorStand ?: return

        if (hand != EquipmentSlot.HAND) return
        if (rightClicked.type != EntityType.ARMOR_STAND) return
        val gearyItem = player.inventory.toGeary()?.itemInMainHand ?: return
        val hat = gearyItem.get<Hat>() ?: return

        armorstand.equipment.helmet = item
        item.subtract(1)
        player.playSound(rightClicked.location, hat.sound, 1f, 1f)

    }
}
