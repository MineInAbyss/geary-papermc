package com.mineinabyss.geary.papermc.features.common.inventory

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.inventory.GearyPlayerInventory

/** Items whose effects apply without being held & worn armor */
fun GearyPlayerInventory.equippedItems(): List<GearyEntity> =
    listOfNotNull(itemInHelmet, itemInChestplate, itemInLeggings, itemInBoots)
