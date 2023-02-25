@file:Suppress("UNREACHABLE_CODE")

package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.datatypes.forEachBit
import com.mineinabyss.geary.datatypes.pop1
import com.mineinabyss.geary.datatypes.setBit
import com.mineinabyss.geary.datatypes.toIntArray
import com.mineinabyss.geary.helpers.NO_ENTITY
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemReference.*
import com.mineinabyss.geary.papermc.tracking.items.cache.GearyItemCache
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.NMSPlayerInventory
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.time.ticks
import com.soywiz.kds.iterators.fastForEachWithIndex
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import org.bukkit.entity.Player
import java.util.*

/**
 * ItemStack instances are super disposable, they don't represent real items. Additionally, tracking items is
 * very inconsistent, so we must cache all components from an item, then periodically check to ensure these items
 * are still there, alongside all the item movement events available to us.
 *
 * ## Process:
 * - An Inventory component stores a cache of items, which we read and compare to actual items in the inventory.
 * - We go through geary items in the inventory and ensure the right items match our existing slots.
 * - If an item is a mismatch, we add it to a list of mismatches
 * - If an item isn't in our cache, we check the mismatches or deserialize it into the cache.
 * - All valid items get re-serialized TODO in the future there should be some form of dirty tag so we aren't unnecessarily serializing things
 */
class InventoryTrackerSystem : RepeatingSystem(interval = 1.ticks) {
    private val TargetScope.player by get<Player>()
    private val TargetScope.itemCache by get<GearyItemCache>()


    override fun TargetScope.tick() {
        refresh(player, itemCache)
    }

    companion object {
        private val logger get() = geary.logger

        // Avoids bukkit items since ItemMeta does a lot of copying which adds overhead
        fun refresh(player: Player, cache: GearyItemCache) {
            val nmsInv = player.toNMS().inventory

            // Map of entity id to bitset of slots that entity was in (this is necessary for prefabs where the same entity may exist in many slots)
            val toRemoveFromCache = Long2LongOpenHashMap()
            // Entities on items in inventory that do not match the entity in cache
            val checkForMove = Array<Exists.Entity?>(GearyItemCache.MAX_SIZE) { null }
            // Remaining items that must create new entities
            val toLoad = Array<NotLoaded?>(GearyItemCache.MAX_SIZE) { null }

            // Go through all slots and check for changes with cache
            nmsInv.forEachSlot { item, slot ->
                val itemReference = cache.getItemReference(item)
                val currEntity = cache[slot]

                fun queueRemoveForCurrent() {
                    if (currEntity != NO_ENTITY) return
                    val currId = currEntity.id.toLong()
                    toRemoveFromCache[currId] = toRemoveFromCache[currId].setBit(slot)
                }

                // Track mismatches with the cache
                when (itemReference) {
                    is None -> queueRemoveForCurrent()
                    is Exists -> {
                        if (currEntity != itemReference.entity) {
                            queueRemoveForCurrent()
                            // If not PlayerInstanced, this item may have been moved from another slot
                            if (itemReference is Exists.Entity)
                                checkForMove[slot] = itemReference
                        } else {
                            // Always lead to an up-to-date reference
                            cache.set(slot, itemReference)
                        }
                    }

                    is NotLoaded -> {
                        queueRemoveForCurrent()
                        toLoad[slot] = itemReference
                    }
                }
            }

            // Check if any changed items that already exist were moved from another slot
            checkForMove.fastForEachWithIndex { slot, entityReference ->
                if (entityReference == null) return@fastForEachWithIndex

                val entityId = entityReference.entity.id.toLong()

                // If the entity wasn't simply moved, we assume it is a duplicate and re-create it.
                if (entityId !in toRemoveFromCache) {
                    cache.createAndSet(slot, NotLoaded.Entity(entityReference.pdc, entityReference.item))
                    return@fastForEachWithIndex
                }

                fun popSingleSlot(): Int {
                    // Pop one slot
                    val entityWasInSlots = toRemoveFromCache[entityId]
                    val popped = entityWasInSlots.pop1()

                    // Update cache as needed. If popped is now empty, remove it
                    if (popped == 0L) toRemoveFromCache.remove(entityId)
                    else toRemoveFromCache[entityId] = popped

                    // Get the index of the popped slot
                    return (entityWasInSlots xor popped).countTrailingZeroBits()
                }

                val oldSlot = popSingleSlot()
                val newReferenceInOldSlot = checkForMove[oldSlot]

                // Swap if the new reference in old slot happens to be looking for the old reference in current slot
                if (newReferenceInOldSlot?.entity == cache[slot]) {
                    cache.swap(oldSlot, slot)
                    checkForMove[oldSlot] = null
                } else cache.move(oldSlot, slot)
                //TODO are we overlooking when newReferenceInOldSlot exists but is not the same as cache[slot]?
                //TODO I think we shouldn't be checking cache, but rather toRemoveFromCache
            }

            // Remove anything that didn't find a new slot to move to
            toRemoveFromCache.forEach { (entityId, slots) ->
                val entity = entityId.toGeary()
                logger.d("Removed $entity from ${player.name} in slots ${slots.toIntArray()}")
                slots.forEachBit {
                    cache.remove(it, removeEntity = true)
                }
            }

            // Add queued up items
            toLoad.fastForEachWithIndex { slot, notLoaded ->
                if (notLoaded == null) return@fastForEachWithIndex
                cache.createAndSet(slot, notLoaded)
            }
        }

        private inline fun NMSPlayerInventory.forEachSlot(action: (NMSItemStack, Int) -> Unit) {
            var slot = 0
            compartments.forEach { comp ->
                comp.forEach { item ->
                    action(item, slot++)
                }
            }
            // Include cursor as last slot
            action(player.containerMenu.carried, GearyItemCache.CURSOR_SLOT)
        }
    }
}
