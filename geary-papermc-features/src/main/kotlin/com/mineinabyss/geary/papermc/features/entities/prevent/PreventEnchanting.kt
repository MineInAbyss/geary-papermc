package com.mineinabyss.geary.papermc.features.entities.prevent

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent

/**
 * Prevents an item from being enchantable in an enchanting table.
 */
@Serializable
@SerialName("geary:prevent.enchanting")
class PreventEnchanting

class PreventEnchantingListener() : Listener {
    @EventHandler
    fun PrepareItemEnchantEvent.disableEnchantPreview() = with(enchanter.world.toGeary()) {
        itemEntityContext {
            if (item.toGearyOrNull()?.has<PreventEnchanting>() == true) {
                isCancelled = true
            }
        }
    }

    fun EnchantItemEvent.disableEnchanting() = with(enchanter.world.toGeary()) {
        itemEntityContext {
            if (item.toGearyOrNull()?.has<PreventEnchanting>() == true) {
                isCancelled = true
            }
        }
    }
}
