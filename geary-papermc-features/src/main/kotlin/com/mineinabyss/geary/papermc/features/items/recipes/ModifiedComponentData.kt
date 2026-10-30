package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

internal sealed class ModifiedComponentData {
    abstract val type: DataComponentType

    abstract fun applyTo(result: ItemStack, source: ItemStack)

    sealed class Parser<T : ModifiedComponentData>(private val verb: String, private val create: (DataComponentType) -> T) {
        fun parse(names: List<String>, context: RecipeContext): List<T> = names.mapNotNull { name ->
            parse(name)?.let(create) ?: context.warn("$verb the unknown or unsupported data component $name")
        }.distinct()
    }

    companion object {
        private val registry by lazy { RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE) }

        /** Null for components Paper cannot read or write, such as custom_data */
        fun parse(name: String): DataComponentType? = NamespacedKey.fromString(name.lowercase())
            ?.let(registry::get)
            ?.takeIf { it is DataComponentType.Valued<*> || it is DataComponentType.NonValued }

        /**
         * The first source listed wins a component several ingredients preserve.
         * A component the source lacks keeps the value [result] defines.
         * Removals run last, so they also drop what another ingredient preserves
         */
        fun applyAll(result: ItemStack, sources: List<Pair<RecipeIngredient, ItemStack?>>): ItemStack {
            val preserved = linkedMapOf<DataComponentType, Pair<PreservedComponentData, ItemStack>>()
            sources.forEach { (ingredient, item) ->
                if (item != null) ingredient.preserved.forEach { preserved.putIfAbsent(it.type, it to item) }
            }

            val output = result.clone()
            // DAMAGE goes last so it is clamped against a preserved MAX_DAMAGE
            preserved.values.sortedBy { (data) -> data.type == DataComponentTypes.DAMAGE }.forEach { (data, item) -> data.applyTo(output, item) }
            sources.forEach { (ingredient, item) -> if (item != null) ingredient.removed.forEach { it.applyTo(output, item) } }
            return output
        }
    }
}

internal data class PreservedComponentData(override val type: DataComponentType) : ModifiedComponentData() {
    override fun applyTo(result: ItemStack, source: ItemStack) {
        when (type) {
            is DataComponentType.NonValued -> if (source.hasData(type)) result.setData(type)
            is DataComponentType.Valued<*> -> copyValued(source, result, type)
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

    companion object : Parser<PreservedComponentData>("preserves", ::PreservedComponentData)
}

internal data class RemovedComponentData(override val type: DataComponentType) : ModifiedComponentData() {
    override fun applyTo(result: ItemStack, source: ItemStack) = result.unsetData(type)

    companion object : Parser<RemovedComponentData>("removes", ::RemovedComponentData)
}
