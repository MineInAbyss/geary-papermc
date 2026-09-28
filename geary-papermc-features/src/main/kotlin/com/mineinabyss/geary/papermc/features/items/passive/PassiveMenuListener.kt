package com.mineinabyss.geary.papermc.features.items.passive

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveInventory
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import com.mineinabyss.idofront.nms.aliases.toNMS
import kotlinx.coroutines.delay
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket
import net.minecraft.world.inventory.RecipeBookType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent

/** Opens the passive inventory as a 3x3 menu from the recipe book button in the player inventory */
class PassiveMenuListener : Listener {
    fun open(player: Player) {
        val passive = PassiveSlots.get(player) ?: return
        player.openInventory(passive.inventory)
    }

    @EventHandler
    fun PlayerRecipeBookSettingsChangeEvent.openOnRecipeBook() {
        if (!isOpen || recipeBookType != PlayerRecipeBookSettingsChangeEvent.RecipeBookType.CRAFTING) return
        if (player.openInventory.type != InventoryType.CRAFTING) return

        // The client already flipped its book open, put it back so the next click sends open again
        val nmsPlayer = player.toNMS()
        nmsPlayer.recipeBook.setOpen(RecipeBookType.CRAFTING, false)
        nmsPlayer.connection.send(ClientboundRecipeBookSettingsPacket(nmsPlayer.recipeBook.bookSettings))
        open(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun InventoryClickEvent.enforceRules() {
        val holder = view.topInventory.holder as? PassiveInventory ?: return
        val player = whoClicked as? Player ?: return
        val passive = PassiveSlots.get(player) ?: return
        if (passive.playerId != holder.playerId) return
        if (PassiveSlotRules.handleClick(this, passive)) saveLater(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun InventoryDragEvent.enforceRules() {
        val holder = view.topInventory.holder as? PassiveInventory ?: return
        val player = whoClicked as? Player ?: return
        val passive = PassiveSlots.get(player) ?: return
        if (passive.playerId != holder.playerId) return
        if (PassiveSlotRules.handleDrag(this, passive)) saveLater(player)
    }

    // The menu can stay open across a vanilla autosave, so the persisted copy must follow every change
    private fun saveLater(player: Player) = gearyPaper.launch {
        delay(1.ticks)
        PassiveSlots.save(player)
    }

    @EventHandler
    fun InventoryCloseEvent.saveOnClose() {
        if (inventory.holder !is PassiveInventory) return
        PassiveSlots.save(player as? Player ?: return)
    }
}
