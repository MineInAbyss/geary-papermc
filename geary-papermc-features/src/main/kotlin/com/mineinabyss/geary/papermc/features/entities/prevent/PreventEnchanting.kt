package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.inventory.ItemStack

/**
 * Prevents an item from being enchantable in an enchanting table.
 */
@Serializable
@SerialName("geary:prevent.enchanting")
class PreventEnchanting

class PreventEnchantingListener() : Listener {
    @EventHandler
    fun PrepareItemEnchantEvent.disableEnchantPreview() {
        if (shouldPrevent(enchanter.world, item)) isCancelled = true
    }

    @EventHandler
    fun EnchantItemEvent.disableEnchanting() {
        if (shouldPrevent(enchanter.world, item)) isCancelled = true
    }

    @EventHandler
    fun PrepareAnvilEvent.disableAnvilEnchant() {
        val world = viewers.firstOrNull()?.world ?: return
        if (shouldPrevent(world, result)) result = null
    }

    @EventHandler
    fun PrepareGrindstoneEvent.disableGrindstone() {
        val world = viewers.firstOrNull()?.world ?: return
        if (shouldPrevent(world, result)) result = null
    }

    fun shouldPrevent(world: World, item: ItemStack?): Boolean {
        if (item == null) return false
        // If an item cannot normally be enchanted, we dont want it to be unenchantable either
        if (item.getData(DataComponentTypes.ENCHANTABLE) == null) return true
        with(world.toGeary()) {
            itemEntityContext {
                if (item.toGearyOrNull()?.has<PreventEnchanting>() == true) {
                    return true
                }
            }
        }
        return false
    }
}
