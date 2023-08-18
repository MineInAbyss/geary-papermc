package com.mineinabyss.geary.papermc.tracking.items.cache

import java.util.*

/**
 * Used to handle the different ways an item may need to be loaded from a slot.
 */
sealed class ItemInfo {
//    abstract val item: NMSItemStack

    class EntityEncoded(val uuid: UUID?) : ItemInfo()

    object ErrorDecoding : ItemInfo()
    object NothingEncoded : ItemInfo()
}
