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
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.nbt.WrappedPDC
import com.mineinabyss.idofront.nms.nbt.fastPDC
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData
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

    /**
     * Like [deserializeItemStackToEntity], but re-encodes into a private copy of the item's PDC.
     * The live tag is shared with every copy of the stack, so changes are only written back through [CustomData.update], and only when the content differs
     */
    fun deserializeNMSItemToEntity(item: NMSItemStack): GearyEntity? {
        val live = item.fastPDC ?: return null
        val scratch = WrappedPDC(live.compoundTag.copy())
        val entity = deserializeItemStackToEntity(scratch)
        if (scratch.compoundTag != live.compoundTag)
            CustomData.update(DataComponents.CUSTOM_DATA, item) { it.put(PDC_KEY, scratch.compoundTag) }
        return entity
    }

    private companion object {
        const val PDC_KEY = "PublicBukkitValues"
    }
}
