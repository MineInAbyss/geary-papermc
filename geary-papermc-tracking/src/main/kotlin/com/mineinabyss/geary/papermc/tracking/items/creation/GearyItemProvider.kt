package com.mineinabyss.geary.papermc.tracking.items.creation

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.*
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemReference.*
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import java.util.*

/**
 * Many helper functions related to creating Looty items.
 */
class GearyItemProvider {
    val logger get() = geary.logger

    /** Creates an ItemStack from a [prefabKey], encoding relevant information to it. */
//    fun createFromPrefab(
//        prefabKey: PrefabKey,
//    ): ItemStack? {
//        val item = ItemStack(Material.AIR)
//        updateItemFromPrefab(item, prefabKey)
//        return item.takeIf { it.type != Material.AIR }
//    }
//
//    fun updateItemFromPrefab(item: ItemStack, prefabKey: PrefabKey) {
//        val prefab = prefabKey.toEntityOrNull() ?: return
//        prefab.get<LootyType>()?.item?.toItemStack(item)
//        item.editMeta {
//            it.persistentDataContainer.encodePrefabs(listOf(prefabKey))
//        }
//    }

//    private fun updateOldLootyItem(pdc: PersistentDataContainer, prefabs: Set<PrefabKey>, item: NMSItemStack) {
//        val tag = item.tag ?: return
//        if (!tag.contains("CustomModelData")) return
//        if (prefabs.isEmpty()) {
//            val prefab = CustomModelDataToPrefabMap[CustomItem(
//                CraftMagicNumbers.getMaterial(item.item),
//                tag.getInt("CustomModelData")
//            )] ?: return
//            pdc.encodeComponents(setOf(), GearyEntityType())
//            pdc.encodePrefabs(listOf(prefab))
//        }
//    }


    //TODO return the instance of prefab for PlayerInstanced
    /** Gets or creates a [GearyEntity] based on a given item and the context it is in. */
    fun newItemEntityOrPrefab(
        holder: GearyEntity,
        reference: NotLoaded
    ): Exists = when (reference) {
        is NotLoaded.Entity -> {
            val decoded = reference.pdc.decodeComponents()
            Exists.Entity(
                entity {
                    addParent(holder)
                    add<RegenerateUUIDOnClash>()
                    loadComponentsFrom(decoded)
                    getOrSetPersisting<UUID> { UUID.randomUUID() }
                    encodeComponentsTo(reference.pdc)
                    logger.d("Loaded new instance of prefab ${get<PrefabKey>()} on $holder")
                },
                reference.pdc,
                reference.item,
            )
        }

        is NotLoaded.PlayerInstanced -> {
            Exists.PlayerInstanced(
                reference.prefab,
                reference.item,
            )
        }
    }
}
