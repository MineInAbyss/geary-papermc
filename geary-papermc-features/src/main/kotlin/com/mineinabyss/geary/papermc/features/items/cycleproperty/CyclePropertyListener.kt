package com.mineinabyss.geary.papermc.features.items.cycleproperty

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot

class CyclePropertyListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEvent.onClick() {
        cycleProperties(player, hand ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onClick() {
        cycleProperties(player, hand)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun PlayerItemConsumeEvent.onConsume() {
        cycleProperties(player, hand)
    }

    fun cycleProperties(player: Player, hand: EquipmentSlot) {
        val itemStack = player.inventory.getItem(hand)
        val heldItem = player.inventory.toGeary()?.get(hand) ?: return
        val cycleProperty = heldItem.get<CycleProperty>() ?: return

        var itemCmd =  itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA) ?: return

        val newFloats = itemCmd.floats().toMutableList()
        val newStrings = itemCmd.strings().toMutableList()
        val newFlags = itemCmd.flags().toMutableList()

        cycleProperty.customModelData.floats.forEachIndexed { index, cycle ->
            if (cycle.isEmpty()) return@forEachIndexed
            val current = newFloats.getOrNull(index) ?: return@forEachIndexed
            newFloats[index] = if (cycle.endInclusive == current) cycle.start else current + 1
        }

        cycleProperty.customModelData.strings.forEachIndexed { index, cycle ->
            if (cycle.isEmpty()) return@forEachIndexed
            val current = newStrings.getOrNull(index) ?: return@forEachIndexed
            val nextIndex = (cycle.indexOf(current) + 1) % cycle.size
            newStrings[index] = cycle.getOrElse(nextIndex) { cycle.first() }
        }

        cycleProperty.customModelData.flags.forEachIndexed { index, shouldToggle ->
            if (shouldToggle == null) return@forEachIndexed
            val current = newFlags.getOrNull(index) ?: return@forEachIndexed
            newFlags[index] = !current
        }

        itemCmd = CustomModelData.customModelData()
            .addFloats(newFloats)
            .addStrings(newStrings)
            .addFlags(newFlags)
            .addColors(itemCmd.colors())
            .build()



        itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, itemCmd)
    }
}