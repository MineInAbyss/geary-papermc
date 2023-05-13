package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.prefabs.PrefabKey
import java.util.UUID

/**
 * Used to handle the different ways an item may need to be loaded from a slot.
 */
sealed class ItemInfo {
//    abstract val item: NMSItemStack

    class EntityEncoded(val uuid: UUID?) : ItemInfo()

    class PlayerInstanced(val prefabs: Set<PrefabKey>) : ItemInfo()

    object ErrorDecoding : ItemInfo()
    object NothingEncoded : ItemInfo()
}
