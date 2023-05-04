package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.*
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo.*
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.nbt.fastPDC
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
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
        prefab.get<SetItem>()?.item?.toItemStack(item)
        item.editMeta {
            it.persistentDataContainer.encodePrefabs(listOf(prefabKey))
        }
        return item.takeIf { it.type != Material.AIR }
    }

    //TODO return the instance of prefab for PlayerInstanced
    /**
     * Creates a new entity from an ItemStack with data encoded to its PDC.
     * This will always create a new entity
     */
    fun deserializeItemStackToEntity(
        reference: NMSItemStack
    ): GearyEntity {
        val pdc = reference.fastPDC
        return entity {
            pdc.decodePrefabs()
//            addParent(holder)
            add<RegenerateUUIDOnClash>()
//            loadComponentsFrom(decoded)
            getOrSetPersisting<UUID> { UUID.randomUUID() }
            encodeComponentsTo(reference.pdc)
            logger.d("Loaded new instance of prefab ${get<PrefabKey>()} on $holder")
        }
    }
}
