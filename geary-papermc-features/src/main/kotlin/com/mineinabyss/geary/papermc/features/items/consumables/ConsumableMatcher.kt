package com.mineinabyss.geary.papermc.features.items.consumables

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.datastore.encodedPrefabs
import com.mineinabyss.geary.papermc.datastore.encodedPrefabsOrNull
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.serialization.SerializableItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Same result as [SerializableItemStack.matches], but first rejects items that cannot equal the built template.
 * When set, [material] and [prefabBytes] are what the built template is guaranteed to end up with
 */
internal class ConsumableMatcher private constructor(
    private val template: SerializableItemStack,
    private val material: Material?,
    private val prefabBytes: ByteArray?,
) {
    fun matches(item: ItemStack): Boolean {
        if (item.isEmpty) return template.matches(item)
        if (material != null && item.type != material) return false
        if (prefabBytes != null && item.persistentDataContainer.encodedPrefabsOrNull()?.contentEquals(prefabBytes) != true) return false
        return template.matches(item)
    }

    companion object {
        context(world: WorldScoped)
        fun of(template: SerializableItemStack): ConsumableMatcher {
            val explicitType = template.type
            if (explicitType != null && !explicitType.isPlainItem()) return ConsumableMatcher(template, null, null)
            val (key, prefabItem) = gearyPrefabItem(template) ?: return ConsumableMatcher(template, explicitType, null)
            val prefabType = prefabItem.type!!
            // A different explicit type is applied after the prefab, which may rewrite the item meta
            val prefabBytes = if (explicitType == null || explicitType == prefabType) encodedPrefabs(listOf(key)) else null
            return ConsumableMatcher(template, explicitType ?: prefabType, prefabBytes)
        }

        // Only prefab items that the Geary provider is certain to apply, without nested providers or an empty result
        context(world: WorldScoped)
        private fun gearyPrefabItem(template: SerializableItemStack): Pair<PrefabKey, SerializableItemStack>? {
            val prefab = template.prefab ?: return null
            if (' ' in prefab || template.itemProvider == null || template.unsetComponents.isNotEmpty()) return null
            val key = PrefabKey.ofOrNull(prefab) ?: return null
            val item = world.entityOfOrNull(key)?.get<SetItem>()?.item ?: return null
            if (item.prefab != null || item.type?.isPlainItem() != true) return null
            if ((item.amount ?: 1) <= 0 || item.unsetComponents.isNotEmpty()) return null
            return key to item
        }

        private fun Material.isPlainItem() = isItem && !isAir
    }
}
