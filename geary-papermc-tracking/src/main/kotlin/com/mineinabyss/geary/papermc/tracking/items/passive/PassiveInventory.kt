package com.mineinabyss.geary.papermc.tracking.items.passive

import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.UUID

/**
 * Live contents of a player's passive slots while they are online.
 * Shown to the player as the passive slots menu, never to other players
 */
class PassiveInventory(
    val playerId: UUID,
    items: PassiveItems,
) : InventoryHolder {
    var unlockedSlots: Int = items.unlockedSlots

    private val backing: Inventory = Bukkit.createInventory(this, InventoryType.DISPENSER, PassiveSlots.config.menuTitle.miniMsg())
        .apply { items.items.forEachIndexed { slot, item -> setItem(slot, item) } }

    val player: Player? get() = Bukkit.getPlayer(playerId)

    override fun getInventory(): Inventory = backing

    fun isUnlocked(slot: Int) = slot in 0 until unlockedSlots

    companion object {
        fun create(player: Player, items: PassiveItems) = PassiveInventory(player.uniqueId, items)
    }
}
