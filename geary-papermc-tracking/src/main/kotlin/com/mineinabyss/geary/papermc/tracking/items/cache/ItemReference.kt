package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.NO_ENTITY
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * Used to handle the different ways an item may need to be loaded from a slot.
 */
sealed class ItemReference {
    abstract val item: NMSItemStack

    sealed class Exists : ItemReference() {
        abstract val entity: GearyEntity

        /** Reference to an already loaded [entity]. */
        class Entity(
            override val entity: GearyEntity,
            val pdc: PersistentDataContainer,
            override val item: NMSItemStack
        ) : Exists()

        /** References an already loaded [entity] which is a [PlayerInstancedItem]. */
        class PlayerInstanced(
            override val entity: GearyEntity,
            override val item: NMSItemStack,
        ) : Exists()
    }

    /** Reference does not exist. */
    class None(override val item: NMSItemStack) : ItemReference()

    sealed class NotLoaded: ItemReference() {
        /** Reference exists but is not loaded yet. */
        class Entity(
            val pdc: PersistentDataContainer,
            override val item: NMSItemStack
        ) : NotLoaded()

        /** Reference to a [PlayerInstancedItem] exists but is not loaded yet. */
        class PlayerInstanced(
            val prefab: PrefabKey,
            override val item: NMSItemStack
        ) : NotLoaded()
    }
}
