package com.mineinabyss.geary.papermc.tracking.items.components

import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

/**
 * The custom_model_data strings an item may switch between, with no custom_model_data being the base look.
 * Item migration keeps one of these on an item rather than resetting it to the prefab's
 */
@JvmInline
@Serializable
@SerialName("geary:custom_model_data_variants")
value class CustomModelDataVariants(val strings: List<String>) {
    fun current(item: ItemStack): String? =
        item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)?.strings()?.firstOrNull()?.takeIf { it in strings }
}
