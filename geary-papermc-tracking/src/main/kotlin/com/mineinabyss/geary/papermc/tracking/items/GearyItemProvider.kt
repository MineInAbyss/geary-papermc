package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOf
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.*

/**
 * Many helper functions related to creating Looty items.
 */
class GearyItemProvider(world: Geary): Geary by world {
    /** Creates an ItemStack from a [prefabKey], encoding relevant information to it. */
    fun serializePrefabToItemStack(prefabKey: PrefabKey, existing: ItemStack? = null): ItemStack? {
        val prefab = prefabKey.toEntityOrNull() ?: return null

        return prefab.get<SetItem>()?.item?.toItemStackOrNull(existing ?: ItemStack(Material.AIR))?.editItemMeta {
            persistentDataContainer.encodePrefabs(listOf(prefabKey))
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
            pdc.decodePrefabs().forEach { extend(entityOf(it)) }
            if (holder != null) addParent(holder)
            loadComponentsFrom(pdc)
            encodeComponentsTo(pdc)
            logger.d("Loaded new instance of prefab ${get<PrefabKey>()}")
        }
    }
}
