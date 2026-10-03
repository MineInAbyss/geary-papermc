package com.mineinabyss.geary.papermc.tracking.items.passive

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.serialization.setPersisting
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object PassiveSlots {
    const val COUNT = 9
    val config get() = gearyPaper.config.items.passiveSlots
    private val fillerKey = NamespacedKey("geary", "passive_filler")

    /** Whether the passive menu rules and saving are running, without them a viewed inventory would never persist */
    var menuEnabled = false

    // Kept until no one views them, so the owner joining in between takes over the same inventory
    private val detached = mutableMapOf<UUID, DetachedPassive>()

    private class DetachedPassive(
        val passive: PassiveInventory,
        val persist: (write: (Player) -> Unit) -> Unit,
        var persisted: PassiveItems,
    )

    /** Index in the player item cache for passive slot [slot] */
    fun cacheSlot(slot: Int) = PlayerItemCache.PASSIVE_SLOT_START + slot

    fun get(player: Player): PassiveInventory? = player.toGearyOrNull()?.get<PassiveInventory>()

    fun load(entity: GearyEntity, player: Player): PassiveInventory {
        val handedOver = detached.remove(player.uniqueId)?.passive
        val passive = handedOver ?: PassiveInventory.create(
            player,
            entity.get<PassiveItems>() ?: PassiveItems(unlockedSlots = config.defaultUnlockedSlots)
        )
        entity.set(passive)
        refreshFillers(passive)
        if (handedOver != null) save(entity, player, passive)
        return passive
    }

    /**
     * [persist] runs after each change to a detached inventory.
     * It must resolve the player data that is current at that moment, pass it to `write` and save it to disk.
     * Holding on to one copy of an offline player is unsafe, other plugins may load and save a newer one meanwhile
     */
    fun inventoryFor(target: Player, persist: (write: (Player) -> Unit) -> Unit): PassiveInventory? {
        if (!menuEnabled) return null
        if (target.isOnline) return get(target)
        detached[target.uniqueId]?.let { return it.passive }

        val items = with(target.world.toGeary()) {
            val pdc = target.persistentDataContainer
            // Showing unreadable data as empty would let the next edit overwrite it
            pdc.decode<PassiveItems>() ?: if (pdc.has<PassiveItems>()) return null else PassiveItems(unlockedSlots = config.defaultUnlockedSlots)
        }
        val passive = PassiveInventory(target.uniqueId, items)
        refreshFillers(passive)
        detached[target.uniqueId] = DetachedPassive(passive, persist, snapshot(passive))
        return passive
    }

    fun isDetached(passive: PassiveInventory): Boolean = detached[passive.playerId]?.passive === passive

    fun isActive(passive: PassiveInventory): Boolean {
        val owner = passive.player
        if (owner != null) return get(owner) === passive
        return isDetached(passive)
    }

    // One whose last save failed stays, so a later save or the owner joining still gets its contents
    fun releaseIfUnused(passive: PassiveInventory) {
        if (!isDetached(passive) || passive.inventory.viewers.isNotEmpty()) return
        save(passive)
        val session = detached[passive.playerId] ?: return
        if (session.isUnsaved()) {
            gearyPaper.logger.w { "Keeping unsaved passive slots of ${passive.playerId} in memory" }
            return
        }
        detached.remove(passive.playerId)
    }

    private fun DetachedPassive.isUnsaved(): Boolean {
        val items = snapshot(passive)
        return items.items != persisted.items || items.unlockedSlots != persisted.unlockedSlots
    }

    fun save(player: Player) {
        val entity = player.toGearyOrNull() ?: return
        val passive = entity.get<PassiveInventory>() ?: return
        save(entity, player, passive)
    }

    fun save(passive: PassiveInventory) {
        val owner = passive.player
        if (owner != null) {
            val entity = owner.toGearyOrNull() ?: return
            if (entity.get<PassiveInventory>() === passive) save(entity, owner, passive)
            return
        }
        val session = detached[passive.playerId]?.takeIf { it.passive === passive } ?: return
        // Each persist rewrites the player's whole data file
        if (!session.isUnsaved()) return
        val items = snapshot(passive)
        runCatching {
            session.persist { holder -> with(holder.world.toGeary()) { holder.persistentDataContainer.encode(items) } }
            session.persisted = items
        }.onFailure { gearyPaper.logger.e { "Failed to save detached passive slots of ${passive.playerId}: ${it.stackTraceToString()}" } }
    }

    // Pending delayed saves never run on shutdown
    fun saveAll() {
        Bukkit.getOnlinePlayers().filter { it.openInventory.topInventory.holder is PassiveInventory }.forEach { it.closeInventory() }
        Bukkit.getOnlinePlayers().forEach(::save)
        detached.values.toList().forEach { save(it.passive) }
    }

    /**
     * Vanilla autosaves player data on its own schedule, so waiting for the quit time encode would let a crash restore an older passive state than the saved inventory and duplicate or lose items
     */
    private fun save(entity: GearyEntity, player: Player, passive: PassiveInventory) {
        val passiveItems = entity.setPersisting(snapshot(passive))
        with(entity.world) { player.persistentDataContainer.encode(passiveItems) }
    }

    private fun snapshot(passive: PassiveInventory) = PassiveItems(
        List(COUNT) { slot ->
            passive.inventory.getItem(slot)?.takeUnless { it.isEmpty || isFiller(it) }?.clone()
        },
        passive.unlockedSlots,
    )

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
