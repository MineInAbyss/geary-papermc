package com.mineinabyss.geary.papermc.features.common.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.inventory.GearyPlayerInventory

/** Inventory slots holding items that apply their effects passively while carried */
val PASSIVE_SLOTS = listOf(9, 10)

/** Items whose effects apply without being held, worn armor and anything in a [PASSIVE_SLOTS] slot */
fun GearyPlayerInventory.equippedItems(): List<GearyEntity> =
    listOfNotNull(itemInHelmet, itemInChestplate, itemInLeggings, itemInBoots) +
            PASSIVE_SLOTS.mapNotNull { slot -> get(slot) }
