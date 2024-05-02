package com.mineinabyss.geary.papermc.features.items.food

import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.random.Random

class FoodConsumptionListener : Listener {

    @EventHandler
    fun PlayerItemConsumeEvent.onConsumeFood() {
        val gearyInventory = player.inventory.toGeary() ?: return
        val entity =
            if (hand == EquipmentSlot.HAND) gearyInventory.itemInMainHand else gearyInventory.itemInOffhand ?: return
        val gearyFood = entity?.get<Food>() ?: return

        if (player.gameMode != GameMode.CREATIVE) {
            player.inventory.getItem(hand).subtract()
            gearyFood.replacement?.toItemStack()?.let { replacement ->
                if (player.inventory.firstEmpty() != -1) player.inventory.addItem(replacement)
                else player.world.dropItemNaturally(player.location, replacement)
            }

            if (gearyFood.effectList.isNotEmpty() && Random.nextDouble(0.0, 1.0) <= gearyFood.effectChance)
                player.addPotionEffects(gearyFood.effectList)
        }

        isCancelled = true
        player.playSound(player.location, Sound.ENTITY_PLAYER_BURP, 1f, 1f)
        player.foodLevel += minOf(gearyFood.hunger, 20)
        player.saturation += minOf(gearyFood.saturation, 20.0).toFloat()
    }
}
