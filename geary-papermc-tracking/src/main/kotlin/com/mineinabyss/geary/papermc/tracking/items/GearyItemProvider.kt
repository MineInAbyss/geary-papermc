package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.*
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemReference.*
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.uuid.components.RegenerateUUIDOnClash
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Many helper functions related to creating Looty items.
 */
class GearyItemProvider {
    val logger get() = geary.logger

    /** Creates an ItemStack from a [prefabKey], encoding relevant information to it. */
    fun createFromPrefab(
        prefabKey: PrefabKey,
    ): ItemStack? {
        val item = ItemStack(Material.AIR)
        updateFromPrefab(item, prefabKey)
        return item.takeIf { it.type != Material.AIR }
    }

    fun updateFromPrefab(item: ItemStack, prefabKey: PrefabKey) {
        val prefab = prefabKey.toEntityOrNull() ?: return
        prefab.get<SetItem>()?.item?.toItemStack(item)
        item.editMeta {
            it.persistentDataContainer.encodePrefabs(listOf(prefabKey))
        }
    }

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
