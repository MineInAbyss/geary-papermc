package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * Many helper functions related to creating Looty items.
 */
class GearyItemProvider(world: Geary) : Geary by world {
    /** Creates an ItemStack from a [prefabKey], encoding relevant information to it. */
    fun serializePrefabToItemStack(prefabKey: PrefabKey, existing: ItemStack? = null): ItemStack? {
        val prefab = entityOfOrNull(prefabKey) ?: return null

        return prefab.get<SetItem>()
            ?.item
            ?.toItemStackOrNull(existing)
            ?.apply {
                editPersistentDataContainer { it.encodePrefabs(listOf(prefabKey)) }
            }
    }

    /**
     * Creates a new entity from an ItemStack with data encoded to its PDC.
     * This will always create a new entity
     */
    fun deserializeItemStackToEntity(
        pdc: PersistentDataContainer?,
        holder: GearyEntity? = null,
    ): GearyEntity? {
        if (pdc == null) return null
        return entity {
            pdc.decodePrefabs().forEach {
                extend(entityOfOrNull(it) ?: error("Item tried to load prefab that doesn't exist: $it"))
            }
            if (holder != null) addParent(holder)
            loadComponentsFrom(pdc)
            encodeComponentsTo(pdc)
            logger.d("Loaded new instance of prefab ${get<PrefabKey>()}")
        }
    }
}
