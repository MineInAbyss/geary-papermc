package com.mineinabyss.geary.papermc.tracking.items.passive

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.serialization.setPersisting
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object PassiveSlots {
    const val COUNT = 9
    val config get() = gearyPaper.config.items.passiveSlots
    private val fillerKey = NamespacedKey("geary", "passive_filler")

    /** Index in the player item cache for passive slot [slot] */
    fun cacheSlot(slot: Int) = PlayerItemCache.PASSIVE_SLOT_START + slot

    fun get(player: Player): PassiveInventory? = player.toGearyOrNull()?.get<PassiveInventory>()

    fun load(entity: GearyEntity, player: Player): PassiveInventory {
        val items = entity.get<PassiveItems>() ?: PassiveItems(unlockedSlots = config.defaultUnlockedSlots)
        return PassiveInventory.create(player, items).also {
            entity.set(it)
            refreshFillers(it)
        }
    }

    /**
     * Writes the live inventory into the persisted component and straight into the player's PDC.
     * Vanilla autosaves player data on its own schedule, so waiting for the quit time encode would
     * let a crash restore an older passive state than the saved inventory and duplicate or lose items
     */
    fun save(player: Player) {
        val entity = player.toGearyOrNull() ?: return
        val passive = entity.get<PassiveInventory>() ?: return
        // Locked slots only ever hold fillers
        val items = List(COUNT) { slot ->
            passive.inventory.getItem(slot)?.takeIf { passive.isUnlocked(slot) && !it.isEmpty }?.clone()
        }
        val passiveItems = entity.setPersisting(PassiveItems(items, passive.unlockedSlots))
        with(entity.world) { player.persistentDataContainer.encode(passiveItems) }
    }

    /**
     * Sets how many slots [player] has unlocked, clamped to 0..[COUNT].
     * Items sitting in slots that become locked go back to the player, dropped at their feet if the inventory is full
     */
    fun setUnlocked(player: Player, count: Int): Boolean {
        val passive = get(player) ?: return false
        val newCount = count.coerceIn(0, COUNT)
        val oldCount = passive.unlockedSlots
        if (newCount == oldCount) return true
        if (!PassiveSlotsChangeEvent(player, oldCount, newCount).callEvent()) return false

        passive.unlockedSlots = newCount
        for (slot in newCount until COUNT) {
            val item = passive.inventory.getItem(slot)?.takeUnless { isFiller(it) } ?: continue
            passive.inventory.setItem(slot, null)
            player.inventory.addItem(item).values.forEach { player.world.dropItemNaturally(player.location, it) }
        }
        refreshFillers(passive)
        save(player)
        return true
    }

    fun unlock(player: Player, amount: Int): Boolean {
        val passive = get(player) ?: return false
        return setUnlocked(player, passive.unlockedSlots + amount)
    }

    fun isFiller(item: ItemStack?) = item != null && !item.isEmpty && item.persistentDataContainer.has(fillerKey)

    fun createFiller(): ItemStack? = config.lockedSlotItem?.toItemStackOrNull()?.apply {
        editPersistentDataContainer { it.set(fillerKey, PersistentDataType.BOOLEAN, true) }
    }

    fun refreshFillers(passive: PassiveInventory) {
        repeat(COUNT) { slot ->
            val item = passive.inventory.getItem(slot)
            if (passive.isUnlocked(slot)) {
                if (isFiller(item)) passive.inventory.setItem(slot, null)
            } else if (item == null || item.isEmpty) passive.inventory.setItem(slot, createFiller())
        }
    }
}
