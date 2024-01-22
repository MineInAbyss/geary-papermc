package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.*

/**
 * Many helper functions related to creating Looty items.
 */
class GearyItemProvider {
    val logger get() = geary.logger

    /** Creates an ItemStack from a [prefabKey], encoding relevant information to it. */
    fun serializePrefabToItemStack(prefabKey: PrefabKey, existing: ItemStack? = null): ItemStack? {
        val item = existing ?: ItemStack(Material.AIR)
        val prefab = prefabKey.toEntityOrNull() ?: return null

        prefab.get<SetItem>()?.item?.toItemStack(
            applyTo = item,
            ignoreProperties = prefab.get<SetItemIgnoredProperties>()?.ignoreAsEnumSet()
                ?: EnumSet.noneOf(BaseSerializableItemStack.Properties::class.java)
        )
        item.editMeta {
            it.persistentDataContainer.encodePrefabs(listOf(prefabKey))
        }
        return item.takeIf { it.type != Material.AIR }
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
            pdc.decodePrefabs().forEach { extend(it.toEntity()) }
            if (holder != null) addParent(holder)
            loadComponentsFrom(pdc)
            encodeComponentsTo(pdc)
            logger.d("Loaded new instance of prefab ${get<PrefabKey>()}")
        }
    }
}
