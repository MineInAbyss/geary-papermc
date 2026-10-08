package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.withGeary
import com.nexomc.nexo.api.NexoItems
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent
import com.nexomc.nexo.api.events.NexoPreItemsLoadEvent
import com.nexomc.nexo.api.events.custom_block.NexoCustomBlockPreDropLootEvent
import com.nexomc.nexo.api.events.furniture.NexoFurnitureDropEvent
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent
import com.nexomc.nexo.nms.NMSHandlers
import com.nexomc.nexo.utils.drops.Drop
import com.nexomc.nexo.utils.drops.Loot
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class NexoListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun NexoCustomBlockPreDropLootEvent.onPreDropLoot() {
        block.withGeary { drop = drop.withGearyItems(itemInHand) }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun NexoFurnitureDropEvent.onDrop() {
        baseEntity.withGeary { drops.replaceAll { it.toGearyItem() ?: it } }
    }

    @EventHandler
    fun NexoItemsLoadedEvent.onItemsLoaded() = registerAxiomPlacerStrip()

    @EventHandler
    fun NexoPreItemsLoadEvent.onPreItemsLoad() {
        val loaded = PrefabLoading.awaitLoaded()
        // While prefabs are still being read the observers register each item as it comes in
        if (!loaded.isDone) return deferUntil(loaded)
        gearyPaper.forEachWorld { registerAllNexoItems() }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun NexoFurniturePlaceEvent.onPlace() {
        val itemPdc = itemInHand.persistentDataContainer
        if (!itemPdc.hasComponentsEncoded) return

        baseEntity.withGeary {
            val entity = baseEntity.toGearyOrNull() ?: return@withGeary
            entity.loadComponentsFrom(itemPdc)
            entity.encodeComponentsTo(baseEntity)
        }
    }
}

context(world: WorldScoped)
private fun Drop.withGearyItems(itemInHand: ItemStack): Drop {
    if (isSilkTouchDrop(itemInHand)) {
        val gearyItem = gearyItem(sourceID) ?: return this
        return copy(isSilktouch = false, loots = mutableListOf(Loot(sourceID, gearyItem, 1.0, 1..1)))
    }
    return copy(loots = loots.mapTo(mutableListOf()) { loot ->
        loot.itemStack().toGearyItem()?.let { loot.copy(itemStack = it) } ?: loot
    })
}

context(world: WorldScoped)
private fun ItemStack.toGearyItem(): ItemStack? {
    if (persistentDataContainer.hasComponentsEncoded) return null
    return gearyItem(NexoItems.idFromItem(this))?.asQuantity(amount)
}

context(world: WorldScoped)
private fun gearyItem(nexoId: String?): ItemStack? {
    val prefabKey = nexoId?.let { world.nexo2Prefab?.get(it) } ?: return null
    return world.world.getAddon(ItemTracking).createItem(prefabKey)
}

private val axiomPlacerStripKey = Key.key("geary", "strip_axiom_placer")

/**
 * Nexo's item-updater writes a custom block's Axiom placer-tag onto every item it updates, so it is taken back off geary items.
 * Nexo clears its update callbacks on every item-load, so this has to be registered again each time
 */
internal fun registerAxiomPlacerStrip() = NexoItems.registerUpdateCallback(axiomPlacerStripKey, { it }) { _, item, _ ->
    if (item.persistentDataContainer.hasComponentsEncoded)
        NMSHandlers.handler().itemUtils().customDataComponent(item, mapOf("Axiom" to null))
    item
}

internal fun unregisterAxiomPlacerStrip() = NexoItems.unregisterUpdateCallback(axiomPlacerStripKey)
