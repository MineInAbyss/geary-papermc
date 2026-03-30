package com.mineinabyss.geary.papermc.tracking.items.cache

/**
 * Used to handle the different ways an item may need to be loaded from a slot.
 */
sealed class ItemInfo {
//    abstract val item: NMSItemStack

    object EntityEncoded : ItemInfo()

    object ErrorDecoding : ItemInfo()
    object NothingEncoded : ItemInfo()
}
