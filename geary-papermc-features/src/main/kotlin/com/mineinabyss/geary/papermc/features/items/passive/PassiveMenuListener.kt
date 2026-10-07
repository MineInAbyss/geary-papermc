package com.mineinabyss.geary.papermc.features.items.passive

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveInventory
import com.mineinabyss.geary.papermc.tracking.items.passive.PassiveSlots
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket
import net.minecraft.world.inventory.RecipeBookType
import net.minecraft.world.item.ItemStack
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
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
        player.openInventory(passive.inventory)?.takeUnless { it.topInventory.holder !== passive } ?: return
        carryCursorOver(player)
    }

    private fun carryCursorOver(player: Player) {
        val nmsPlayer = player.toNMS()
        val playerInv = nmsPlayer.inventoryMenu.takeUnless { it.carried.isEmpty } ?: return
        val passiveInv = nmsPlayer.containerMenu.takeUnless { playerInv === it } ?: return

        val playerCursor = playerInv.carried
        playerInv.carried = ItemStack.EMPTY
        passiveInv.carried = playerCursor
        passiveInv.broadcastCarriedItem()
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun InventoryClickEvent.enforceRules() {
        val passive = view.topInventory.holder as? PassiveInventory ?: return
        val player = whoClicked as? Player ?: return
        if (!PassiveSlots.isActive(passive)) {
            isCancelled = true
            return
        }
        if (PassiveSlotRules.handleClick(this, passive)) saveLater(passive, player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun InventoryDragEvent.enforceRules() {
        val passive = view.topInventory.holder as? PassiveInventory ?: return
        val player = whoClicked as? Player ?: return
        if (!PassiveSlots.isActive(passive)) {
            isCancelled = true
            return
        }
        if (PassiveSlotRules.handleDrag(this, passive)) saveLater(passive, player)
    }

    private class PendingSave(val clickTick: Int, val clickers: MutableSet<Player> = mutableSetOf())

    private val pendingSaves = mutableMapOf<PassiveInventory, PendingSave>()

    // The menu can stay open across a vanilla autosave, so the persisted copy must follow every change
    private fun saveLater(passive: PassiveInventory, clicker: Player) {
        val pending = pendingSaves[passive]
        if (pending != null) {
            pending.clickers += clicker
            return
        }
        pendingSaves[passive] = PendingSave(Bukkit.getCurrentTick(), mutableSetOf(clicker))
        Bukkit.getScheduler().runTask(gearyPaper, Runnable {
            val save = pendingSaves.remove(passive) ?: return@Runnable
            PassiveSlots.save(passive)
            flushToDisk(passive, save)
        })
    }

    // Staff and owner files are autosaved minutes apart, so a crash could leave a moved item in both or neither.
    // An owner autosave since the click wrote their inventory next to the old passive data
    private fun flushToDisk(passive: PassiveInventory, save: PendingSave) {
        val staff = save.clickers.filter { it.uniqueId != passive.playerId && it.isOnline }
        staff.forEach { it.saveData() }
        val owner = passive.player ?: return
        if (staff.isNotEmpty() || owner.toNMS().lastSave >= save.clickTick) owner.saveData()
    }

    @EventHandler
    fun InventoryCloseEvent.saveOnClose() {
        val passive = inventory.holder as? PassiveInventory ?: return
        PassiveSlots.save(passive)
        // The closing player is still listed as a viewer until the event finishes
        Bukkit.getScheduler().runTask(gearyPaper, Runnable { PassiveSlots.releaseIfUnused(passive) })
    }
}
