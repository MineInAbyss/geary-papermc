package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

internal object PreservedComponents {
    private val registry by lazy { RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE) }

    /** Null for components Paper cannot read or write, such as custom_data */
    fun parse(name: String): DataComponentType? = NamespacedKey.fromString(name.lowercase())
        ?.let(registry::get)
        ?.takeIf { it is DataComponentType.Valued<*> || it is DataComponentType.NonValued }

    /**
     * The first source listed wins a component several ingredients preserve.
     * A component the source lacks keeps the value [result] defines
     */
    fun copyOnto(result: ItemStack, sources: List<Pair<RecipeIngredient, ItemStack?>>): ItemStack {
        val from = linkedMapOf<DataComponentType, ItemStack>()
        sources.forEach { (ingredient, item) ->
            if (item != null) ingredient.preserved.forEach { from.putIfAbsent(it, item) }
        }

        val output = result.clone()
        // DAMAGE goes last so it is clamped against a preserved MAX_DAMAGE
        from.entries.sortedBy { it.key == DataComponentTypes.DAMAGE }.forEach { (type, item) -> copyComponent(item, output, type) }
        return output
    }

    private fun copyComponent(from: ItemStack, to: ItemStack, type: DataComponentType) {
        when (type) {
            is DataComponentType.NonValued -> if (from.hasData(type)) to.setData(type)
            is DataComponentType.Valued<*> -> copyValued(from, to, type)
        }
    }

    private fun <T : Any> copyValued(from: ItemStack, to: ItemStack, type: DataComponentType.Valued<T>) {
        val value = from.getData(type) ?: return
        to.setData(type, merged(to, type, value) ?: return)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> merged(to: ItemStack, type: DataComponentType.Valued<T>, value: T): T? = when {
        value is ItemEnchantments -> {
            val levels = (to.getData(type) as? ItemEnchantments)?.enchantments().orEmpty().toMutableMap()
            value.enchantments().forEach { (enchantment, level) -> levels.merge(enchantment, level) { own, preserved -> maxOf(own, preserved) } }
            ItemEnchantments.itemEnchantments(levels) as T
        }

        type == DataComponentTypes.DAMAGE -> {
            val maxDamage = to.getData(DataComponentTypes.MAX_DAMAGE) ?: return null
            (value as Int).coerceAtMost(maxDamage - 1) as T
        }

        else -> value
    }
}

internal fun List<String>.toPreservedComponents(context: RecipeContext): List<DataComponentType> = mapNotNull { name ->
    PreservedComponents.parse(name) ?: context.warn("preserves the unknown or unsupported data component $name")
}.distinct()
