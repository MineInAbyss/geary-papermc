package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.gets
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins
import com.nexomc.nexo.api.NexoItems
import org.bukkit.configuration.ConfigurationSection

/**
 * Lets prefabs define Nexo items, so a [NexoFurniture] or [NexoCustomBlock] component registers its
 * mechanic the same way an item config in Nexo's items-folder would.
 */
val NexoFeature = module("nexo") {
    requirePlugins("Nexo")

    listeners(NexoFurnitureListener(), NexoItemLoadListener())

    val nexo2Prefab by single { Nexo2Prefab() }

    gearyWorld {
        fun GearyEntity.register(
            prefabKey: PrefabKey,
            setItem: SetItem?,
            mechanic: String,
            section: () -> ConfigurationSection,
        ) {
            val nexoId = nexoId(prefabKey)
            val nexoPrefab = "nexo $nexoId"
            nexo2Prefab[nexoId] = prefabKey
            // Setting the item below retriggers the observer, nothing left to do on that pass
            if ((setItem == null || setItem.item.prefab == nexoPrefab) && NexoItems.exists(nexoId)) return

            registerNexoItem(prefabKey, mechanic, section())

            // Base the item on its own Nexo entry, so items geary hands out carry Nexo's id and its mechanic applies
            if (setItem != null && setItem.item.prefab != nexoPrefab) set(SetItem(setItem.item.copy(prefab = nexoPrefab)))
        }

        observe<OnSet>()
            .involving(query<NexoFurniture, PrefabKey, SetItem>())
            .exec { (furniture, prefabKey, setItem) ->
                entity.register(prefabKey, setItem, "furniture") { furniture.toItemSection(prefabKey, setItem.item.type, setItem.item.itemModel) }
            }

        // SetItem is left out of the query, the children of a directional block are blocks with no item of
        // their own and Nexo still has to know them, a parent resolves its x/y/z_block against registered ids
        observe<OnSet>()
            .involving(query<NexoCustomBlock, PrefabKey>())
            .exec { (block, prefabKey) ->
                val setItem = entity.get<SetItem>()
                entity.register(prefabKey, setItem, "custom_block") {
                    block.toItemSection(prefabKey, setItem?.item?.type, setItem?.item?.itemModel)
                }
            }
    }

    addCloseable { NexoItems.unregisterExternalItems(nexoOwner) }
}.gets<Nexo2Prefab>()
