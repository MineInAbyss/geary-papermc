package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

/**
 * Used to handle the different ways an item may need to be loaded from a slot.
 */
sealed class ItemInfo {
//    abstract val item: NMSItemStack

    class EntityEncoded(val id: GearyEntity) : ItemInfo()

    class PlayerInstanced(val prefabs: Set<PrefabKey>) : ItemInfo()

    object ErrorDecoding : ItemInfo()
    object NothingEncoded : ItemInfo()
}
